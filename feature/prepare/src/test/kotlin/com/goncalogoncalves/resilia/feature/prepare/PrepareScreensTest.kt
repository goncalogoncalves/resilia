package com.goncalogoncalves.resilia.feature.prepare

import androidx.compose.material3.Surface
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasScrollAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToNode
import com.goncalogoncalves.resilia.core.designsystem.theme.ResiliaTheme
import com.goncalogoncalves.resilia.core.domain.model.ContentBundle
import com.goncalogoncalves.resilia.core.domain.model.ContentManifest
import com.goncalogoncalves.resilia.core.domain.model.Household
import com.goncalogoncalves.resilia.core.domain.model.InventoryItem
import com.goncalogoncalves.resilia.core.domain.model.KitCatalog
import com.goncalogoncalves.resilia.core.domain.model.KitCondition
import com.goncalogoncalves.resilia.core.domain.model.KitItem
import com.goncalogoncalves.resilia.core.domain.model.KitList
import com.goncalogoncalves.resilia.core.domain.model.KitQuantity
import com.goncalogoncalves.resilia.core.domain.model.KitQuantityKind
import com.goncalogoncalves.resilia.core.domain.model.KitUnit
import com.goncalogoncalves.resilia.core.domain.model.LocalizedText
import com.goncalogoncalves.resilia.core.domain.model.Source
import com.goncalogoncalves.resilia.core.domain.model.SourceEntity
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.time.LocalDate

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35], qualifiers = "en")
class PrepareScreensTest {

    @get:Rule val compose = createComposeRule()


    private fun t(en: String) = LocalizedText.of("pt $en", en)

    private val bundle = ContentBundle(
        manifest = ContentManifest("0.3.0", 1, emptyList(), "s", "c", "k", "p", "b"),
        sources = listOf(Source("dgs", SourceEntity.DGS, t("DGS page"), "https://example.org", "2026-11-20")),
        scenarios = emptyList(),
        kit = KitCatalog(
            minimumDays = 3,
            minimumDaysSourceIds = listOf("dgs"),
            dayOptions = listOf(3, 7),
            reviewIntervalMonths = 6,
            reviewSourceIds = listOf("dgs"),
            items = listOf(
                KitItem("home.water", KitList.HOME, name = t("Drinking water"), sourceIds = listOf("dgs"),
                    quantity = KitQuantity(KitQuantityKind.PER_PERSON_PER_DAY, 2.0, KitUnit.LITRE)),
                KitItem("home.food", KitList.HOME, name = t("Food that keeps"), sourceIds = listOf("dgs"),
                    quantity = KitQuantity(KitQuantityKind.FOR_DURATION)),
                KitItem("home.baby", KitList.HOME, KitCondition.BABY, name = t("Nappies"), sourceIds = listOf("dgs")),
                KitItem("bag.cash", KitList.BAG, name = t("Cash"), sourceIds = listOf("dgs")),
            ),
        ),
    )

    private fun setCalculator(household: Household, onChange: (Household) -> Unit = {}, added: MutableList<String> = mutableListOf()) =
        compose.setContent {
            ResiliaTheme {
                Surface {
                    KitCalculatorContent(
                        state = KitUiState(bundle, household, inInventory = emptySet()),
                        onChange = onChange,
                        onAdd = { line, _ -> added += line.item.id },
                        onOpenInventory = {},
                    )
                }
            }
        }

    private fun scrollTo(text: String) {
        compose.onNode(hasScrollAction()).performScrollToNode(hasText(text, substring = true))
    }

    @Test
    fun `water is calculated from the official figure`() {
        setCalculator(Household(people = 4, days = 3))
        scrollTo("24 L")
        compose.onNodeWithText("24 L (2 L per person per day)").assertIsDisplayed()
        scrollTo("Enough for")
        compose.onNodeWithText("Enough for 4 people for 3 days").assertIsDisplayed()
    }

    @Test
    fun `baby items appear only when there is a baby`() {
        setCalculator(Household(hasBaby = true))
        scrollTo("Nappies")
        compose.onNodeWithText("Nappies").assertIsDisplayed()
    }

    @Test
    fun `people stepper and add to inventory`() {
        var changed: Household? = null
        val added = mutableListOf<String>()
        setCalculator(Household(people = 2), onChange = { changed = it }, added = added)
        compose.onNodeWithContentDescriptionSafe("One person more").performClick()
        assertEquals(3, changed?.people)
        scrollTo("Add to my supplies")
        compose.onAllNodes(hasText("Add to my supplies"))[0].performClick()
        assertEquals(listOf("home.water"), added)
    }

    private fun androidx.compose.ui.test.junit4.ComposeContentTestRule.onNodeWithContentDescriptionSafe(d: String) =
        onNode(androidx.compose.ui.test.hasContentDescription(d))

    private val today = LocalDate.of(2026, 11, 20)

    private fun setInventory(items: List<InventoryItem>) = compose.setContent {
        ResiliaTheme {
            Surface {
                InventoryContent(
                    state = InventoryUiState(
                        items = items,
                        today = today,
                        remindersEnabled = false,
                        nextReview = if (items.isEmpty()) null else today.plusMonths(6),
                        reviewSources = bundle.sources,
                    ),
                    onSave = {}, onDelete = {}, onMarkReviewed = {}, onSetReminders = {},
                )
            }
        }
    }

    @Test
    fun `empty inventory explains what to do`() {
        setInventory(emptyList())
        scrollTo("Nothing here yet")
        compose.onNodeWithText("Nothing here yet", substring = true).assertIsDisplayed()
    }

    @Test
    fun `inventory shows expiry status`() {
        setInventory(
            listOf(
                InventoryItem(id = 1, name = "Water", expiresOn = today.plusDays(10), addedOn = today),
                InventoryItem(id = 2, name = "Tins", expiresOn = today.minusDays(2), addedOn = today),
            ),
        )
        scrollTo("Expires in 10 days")
        compose.onNodeWithText("Expires in 10 days", substring = true).assertIsDisplayed()
        scrollTo("Expired 2 days ago")
        compose.onNodeWithText("Expired 2 days ago", substring = true).assertIsDisplayed()
        scrollTo("Next review")
        compose.onNodeWithText("Next review", substring = true).assertIsDisplayed()
    }

    @Test
    fun `turning reminders on explains the permission first`() {
        setInventory(emptyList())
        scrollTo("Reminders")
        compose.onNodeWithText("Reminders").performClick()
        compose.onNodeWithText("Allow notifications?").assertIsDisplayed()
    }
}
