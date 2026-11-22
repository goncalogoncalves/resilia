package com.goncalogoncalves.resilia.feature.home

import androidx.compose.material3.Surface
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import com.goncalogoncalves.resilia.core.designsystem.theme.ResiliaTheme
import com.goncalogoncalves.resilia.core.domain.model.AlertLevel
import com.goncalogoncalves.resilia.core.domain.model.LocalizedText
import com.goncalogoncalves.resilia.core.domain.model.Warning
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.time.Instant

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35], qualifiers = "en-w411dp-h2400dp")
class HomeAlertStripTest {

    @get:Rule val compose = createComposeRule()


    @Test
    fun `home shows the orange warning in words`() {
        val t = { s: String -> LocalizedText.of(s, s) }
        val now = Instant.now()
        val alert = HomeAlert(
            area = t("Porto"),
            top = Warning("PTO", "Vento", AlertLevel.ORANGE, now, now.plusSeconds(3600), ""),
            levelName = t("Orange"),
            typeName = t("Wind"),
        )
        compose.setContent {
            ResiliaTheme { Surface { HomeContent(emptyList(), alert, {}, {}, {}, {}, {}) } }
        }
        compose.onNodeWithText("Orange warning").assertExists()
    }
}
