package com.goncalogoncalves.resilia.feature.prepare

import androidx.compose.material3.Surface
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasScrollAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.ui.test.performTextInput
import com.goncalogoncalves.resilia.core.designsystem.theme.ResiliaTheme
import com.goncalogoncalves.resilia.core.domain.model.ContentBundle
import com.goncalogoncalves.resilia.core.domain.model.ContentManifest
import com.goncalogoncalves.resilia.core.domain.model.FamilyPlan
import com.goncalogoncalves.resilia.core.domain.model.FamilyPlanGuide
import com.goncalogoncalves.resilia.core.domain.model.LocalizedText
import com.goncalogoncalves.resilia.core.domain.model.MedicalInfo
import com.goncalogoncalves.resilia.core.domain.model.Member
import com.goncalogoncalves.resilia.core.domain.model.PlanContact
import com.goncalogoncalves.resilia.core.domain.model.PlanSection
import com.goncalogoncalves.resilia.core.domain.model.PlanSectionId
import com.goncalogoncalves.resilia.core.domain.model.Source
import com.goncalogoncalves.resilia.core.domain.model.SourceEntity
import com.goncalogoncalves.resilia.feature.prepare.plan.EmergencyPlanContent
import com.goncalogoncalves.resilia.feature.prepare.plan.FamilyPlanContent
import com.goncalogoncalves.resilia.feature.prepare.plan.FamilyPlanUiState
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35], qualifiers = "en")
class FamilyPlanScreenTest {

    @get:Rule val compose = createComposeRule()


    private val bundle = ContentBundle(
        manifest = ContentManifest("0.4.0", 1, emptyList(), "s", "c", "k", "p", "b"),
        sources = listOf(
            Source("cm", SourceEntity.MUNICIPAL, LocalizedText.of("t", "t"), "https://example.org", "2026-11-21", publisher = "Câmara Municipal de Matosinhos"),
        ),
        scenarios = emptyList(),
        plan = FamilyPlanGuide(
            PlanSectionId.ALL.map { id ->
                if (id == PlanSectionId.MEETING) {
                    PlanSection(id, LocalizedText.of("pt", "Agree on two meeting points."), listOf("cm"))
                } else PlanSection(id)
            },
        ),
    )

    private val rui = Member(id = 1, name = "Rui", medical = MedicalInfo(allergies = "Penicillin"))

    private fun setContent(
        plan: FamilyPlan = FamilyPlan(),
        lock: Boolean = false,
        unlocked: Boolean = false,
        onPlanChange: (FamilyPlan) -> Unit = {},
        onUnlock: () -> Unit = {},
    ) = compose.setContent {
        ResiliaTheme {
            Surface {
                FamilyPlanContent(
                    state = FamilyPlanUiState(bundle, plan, listOf(rui), emptyList(), lock, unlocked),
                    onPlanChange = onPlanChange,
                    onSaveMember = {}, onDeleteMember = {}, onSaveContact = {}, onDeleteContact = {},
                    onUnlock = onUnlock,
                    lockAvailable = true,
                    onSetLock = {},
                    onOpenPaper = {},
                )
            }
        }
    }

    private fun scrollTo(text: String) =
        compose.onNode(hasScrollAction()).performScrollToNode(hasText(text, substring = true))

    @Test
    fun `section hint shows its municipal source`() {
        setContent()
        scrollTo("Agree on two meeting points.")
        compose.onNodeWithText("Agree on two meeting points.").assertIsDisplayed()
        scrollTo("Source: Câmara")
        compose.onNodeWithText("Source: Câmara Municipal de Matosinhos").assertIsDisplayed()
    }

    @Test
    fun `typing a meeting point updates the plan`() {
        var latest: FamilyPlan? = null
        setContent(onPlanChange = { latest = it })
        scrollTo("Near home")
        compose.onNode(hasText("Near home")).performTextInput("Park gate")
        assertEquals("Park gate", latest?.meetingNear)
    }

    @Test
    fun `locked medical sheet is not shown until unlocked`() {
        var unlockAsked = false
        setContent(lock = true, onUnlock = { unlockAsked = true })
        scrollTo("Rui")
        compose.onNodeWithText("Medical sheet locked").assertIsDisplayed()
        compose.onNodeWithText("Rui").performClick()
        compose.onNodeWithText("Show medical sheet").assertExists()
        assertEquals(0, compose.onAllNodes(hasText("Penicillin")).fetchSemanticsNodes().size)
        compose.onNodeWithText("Show medical sheet").performScrollTo().performClick()
        assertEquals(true, unlockAsked)
    }

    @Test
    fun `unlocked medical sheet can be edited`() {
        setContent(lock = true, unlocked = true)
        scrollTo("Rui")
        compose.onNodeWithText("Rui").performClick()
        compose.onNode(hasText("Penicillin")).assertExists()
    }

    @Test
    fun `emergency sheet shows meeting points and contact person`() {
        compose.setContent {
            ResiliaTheme {
                EmergencyPlanContent(
                    plan = FamilyPlan(meetingNear = "Park gate", contactName = "Aunt Ana", contactPhone = "910 000 000"),
                    contacts = listOf(PlanContact(name = "School", phone = "210 000 000")),
                    onClose = {},
                )
            }
        }
        compose.onNodeWithText("Park gate").assertIsDisplayed()
        compose.onNodeWithText("Aunt Ana").assertIsDisplayed()
        compose.onNodeWithText("910 000 000").assertIsDisplayed()
    }
}
