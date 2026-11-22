package com.goncalogoncalves.resilia.feature.morale

import androidx.compose.material3.Surface
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import com.goncalogoncalves.resilia.core.designsystem.theme.ResiliaTheme
import com.goncalogoncalves.resilia.core.domain.model.Activity
import com.goncalogoncalves.resilia.core.domain.model.ActivityFilter
import com.goncalogoncalves.resilia.core.domain.model.ActivityOrigin
import com.goncalogoncalves.resilia.core.domain.model.ActivitySetting
import com.goncalogoncalves.resilia.core.domain.model.ActivityType
import com.goncalogoncalves.resilia.core.domain.model.LightNeed
import com.goncalogoncalves.resilia.core.domain.model.LocalizedText
import com.goncalogoncalves.resilia.core.domain.model.Noise
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35], qualifiers = "en-w411dp-h2400dp")
class MoraleScreenTest {

    @get:Rule val compose = createComposeRule()


    private fun activity(id: String, title: String, light: LightNeed) = Activity(
        id = id, type = ActivityType.GAME, title = LocalizedText.of(title, title), summary = LocalizedText.of("s", "s"),
        ageMin = 4, peopleMin = 2, durationMin = 10, setting = ActivitySetting.BOTH, noise = Noise.QUIET, light = light,
        instructions = listOf(LocalizedText.of("i", "i")), origin = ActivityOrigin.TRADITIONAL,
    )

    private val list = listOf(activity("a", "Who am I?", LightNeed.NONE), activity("b", "Battleships", LightNeed.LIGHT))

    @Test
    fun `dark filter keeps only activities that work with the lights out`() {
        var filter = ActivityFilter()
        compose.setContent {
            ResiliaTheme {
                Surface {
                    MoraleContent(list, ActivityFilter(dark = true), onFilter = { filter = it }, onOpenSupport = {}, onOpenActivity = {})
                }
            }
        }
        compose.onNodeWithText("Who am I?").assertIsDisplayed()
        assertEquals(0, compose.onAllNodes(hasText("Battleships")).fetchSemanticsNodes().size)
        compose.onNodeWithText("Emotional support").assertIsDisplayed()
        assertEquals(ActivityFilter(), filter)
    }
}
