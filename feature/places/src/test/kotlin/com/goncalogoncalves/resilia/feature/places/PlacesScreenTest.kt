package com.goncalogoncalves.resilia.feature.places

import androidx.compose.material3.Surface
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import com.goncalogoncalves.resilia.core.designsystem.theme.ResiliaTheme
import com.goncalogoncalves.resilia.core.domain.model.ContentBundle
import com.goncalogoncalves.resilia.core.domain.model.ContentManifest
import com.goncalogoncalves.resilia.core.domain.model.Hospital
import com.goncalogoncalves.resilia.core.domain.model.HospitalsContent
import com.goncalogoncalves.resilia.core.domain.model.LatLng
import com.goncalogoncalves.resilia.core.domain.model.Place
import com.goncalogoncalves.resilia.core.domain.model.PlaceType
import com.goncalogoncalves.resilia.core.domain.model.UrgencyLevel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35], qualifiers = "en-w411dp-h2400dp")
class PlacesScreenTest {

    @get:Rule val compose = createComposeRule()


    @Test
    fun `places show distance and direction from home`() {
        val places = listOf(
            Place(id = 1, type = PlaceType.HOME, name = "Home", position = LatLng(38.7223, -9.1393)),
            Place(id = 2, type = PlaceType.MEETING, name = "Park", address = "Largo 1", position = LatLng(38.7403, -9.1393)),
        )
        var opened: Any? = null
        compose.setContent { ResiliaTheme { Surface { PlacesContent(places) { opened = it } } } }
        compose.onNodeWithText("Park").assertIsDisplayed()
        compose.onNodeWithText("2.0 km north of home").assertIsDisplayed()
        compose.onNodeWithText("Add a place").performClick()
        assertEquals(PlaceEditRoute(), opened)
    }

    @Test
    fun `hospital search ignores accents and case`() {
        fun h(id: String, name: String, locality: String, region: String) =
            Hospital(id, name, "", UrgencyLevel.BASIC, "Rua 1", "1000-001", locality, region)
        val bundle = ContentBundle(
            manifest = ContentManifest("0", 1, emptyList(), "s", "c", "k", "p", "b"),
            sources = emptyList(),
            scenarios = emptyList(),
            hospitals = HospitalsContent(
                sourceIds = emptyList(),
                dataModified = "2026-06-17",
                retrievedAt = "2026-11-21",
                hospitals = listOf(h("a", "H. Évora", "Évora", "ARS Alentejo"), h("b", "H. Braga", "Braga", "ARS Norte")),
            ),
        )
        compose.setContent { ResiliaTheme { Surface { HospitalsContent(bundle) } } }
        compose.onNodeWithText("ARS Alentejo").assertIsDisplayed()
        compose.onNodeWithText("Search by name, town or postcode").performTextInput("EVORA")
        compose.onNodeWithText("H. Évora").assertIsDisplayed()
        assertTrue(compose.onAllNodes(hasText("H. Braga")).fetchSemanticsNodes().isEmpty())
    }
}
