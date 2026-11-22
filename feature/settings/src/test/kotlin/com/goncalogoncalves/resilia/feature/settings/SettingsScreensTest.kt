package com.goncalogoncalves.resilia.feature.settings

import androidx.compose.material3.Surface
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.goncalogoncalves.resilia.core.designsystem.theme.ResiliaTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35], qualifiers = "en-w411dp-h2400dp")
class SettingsScreensTest {

    @get:Rule val compose = createComposeRule()


    @Test
    fun `settings list opens export and privacy`() {
        val opened = mutableListOf<Any>()
        compose.setContent { ResiliaTheme { Surface { SettingsScreen({}, {}, {}, { opened += it }) } } }
        compose.onNodeWithText("Export and import data").performClick()
        compose.onNodeWithText("Privacy").performClick()
        assertEquals(listOf<Any>(BackupRoute, PrivacyRoute), opened)
    }

    @Test
    fun `privacy text says what leaves the phone`() {
        compose.setContent { ResiliaTheme { Surface { PrivacyScreen() } } }
        compose.onNodeWithText("IP address", substring = true).assertExists()
    }
}
