package com.goncalogoncalves.resilia

import com.goncalogoncalves.resilia.core.domain.Disclaimer
import com.goncalogoncalves.resilia.core.domain.repository.AppState
import org.junit.Assert.assertEquals
import org.junit.Test

class RootModeTest {
    private val accepted = Disclaimer.VERSION

    @Test fun `loading while state unknown`() = assertEquals(RootMode.LOADING, rootModeFor(null))

    @Test fun `disclaimer first, even in emergency`() = assertEquals(
        RootMode.DISCLAIMER,
        rootModeFor(AppState(disclaimerAcceptedVersion = 0, emergencyModeActive = true)),
    )

    @Test fun `emergency when active`() = assertEquals(
        RootMode.EMERGENCY,
        rootModeFor(AppState(disclaimerAcceptedVersion = accepted, emergencyModeActive = true)),
    )

    @Test fun `normal otherwise`() = assertEquals(
        RootMode.NORMAL,
        rootModeFor(AppState(disclaimerAcceptedVersion = accepted)),
    )
}
