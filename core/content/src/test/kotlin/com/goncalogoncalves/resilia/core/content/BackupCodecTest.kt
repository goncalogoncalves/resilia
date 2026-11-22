package com.goncalogoncalves.resilia.core.content

import com.goncalogoncalves.resilia.core.domain.model.BackupCodec
import com.goncalogoncalves.resilia.core.domain.model.BackupData
import com.goncalogoncalves.resilia.core.domain.model.BackupException
import com.goncalogoncalves.resilia.core.domain.model.BackupPlace
import com.goncalogoncalves.resilia.core.domain.model.FamilyPlan
import com.goncalogoncalves.resilia.core.domain.model.Household
import com.goncalogoncalves.resilia.core.domain.model.MedicalInfo
import com.goncalogoncalves.resilia.core.domain.model.Member
import com.goncalogoncalves.resilia.core.domain.model.BackupInventoryItem
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class BackupCodecTest {

    // Fewer iterations than the app uses (600 000), only to keep the test fast.
    private val fast = 1_000

    private val data = BackupData(
        createdAt = "2026-11-22T10:00:00",
        household = Household(people = 3, hasPets = true),
        inventory = listOf(BackupInventoryItem("Água", "6 L", expiresOn = "2027-01-01", addedOn = "2026-11-22")),
        plan = FamilyPlan(meetingNear = "Jardim"),
        members = listOf(Member(name = "Rui", medical = MedicalInfo(allergies = "Penicilina"))),
        places = listOf(BackupPlace("HOME", "Casa", lat = 38.7, lng = -9.1)),
    )

    @Test
    fun `encrypted round trip keeps everything and hides the medical sheet`() {
        val text = BackupCodec.encode(data, "uma-frase-longa".toCharArray(), fast)
        assertFalse("medical data in clear", "Penicilina" in text)
        assertFalse("plan in clear", "Jardim" in text)
        val back = BackupCodec.decode(text, "uma-frase-longa".toCharArray())
        assertEquals(data, back)
        assertEquals("Água", back.inventoryItems().single().name)
        assertEquals(38.7, back.placeItems().single().position?.lat ?: 0.0, 1e-9)
    }

    @Test
    fun `medical sheets are never exported without a password`() {
        assertTrue(runCatching { BackupCodec.encode(data, null) }.exceptionOrNull() is BackupException.MedicalNeedsPassword)
        val noMedical = data.copy(members = listOf(Member(name = "Ana")))
        val text = BackupCodec.encode(noMedical, null)
        assertFalse(BackupCodec.inspect(text).encrypted)
        assertEquals(noMedical, BackupCodec.decode(text, null))
    }

    @Test
    fun `wrong password, tampering and foreign files change nothing`() {
        val text = BackupCodec.encode(data, "uma-frase-longa".toCharArray(), fast)
        assertTrue(BackupCodec.inspect(text).encrypted)
        assertTrue(runCatching { BackupCodec.decode(text, null) }.exceptionOrNull() is BackupException.PasswordRequired)
        assertTrue(runCatching { BackupCodec.decode(text, "outra-frase-longa".toCharArray()) }.exceptionOrNull() is BackupException.WrongPassword)
        val tampered = text.replace(Regex("\"ciphertext\":\"(.)")) { m -> "\"ciphertext\":\"" + (if (m.groupValues[1] == "A") "B" else "A") }
        assertTrue(runCatching { BackupCodec.decode(tampered, "uma-frase-longa".toCharArray()) }.exceptionOrNull() is BackupException.WrongPassword)
        assertTrue(runCatching { BackupCodec.decode("{\"hello\":1}", null) }.exceptionOrNull() is BackupException.NotABackup)
        assertTrue(runCatching { BackupCodec.decode("not json", null) }.exceptionOrNull() is BackupException.NotABackup)
        val newer = BackupCodec.encode(data.copy(members = emptyList()), null).replace("\"version\":1", "\"version\":99")
        assertTrue(runCatching { BackupCodec.decode(newer, null) }.exceptionOrNull() is BackupException.NewerVersion)
    }

    @Test
    fun `short passwords are refused`() {
        assertTrue(runCatching { BackupCodec.encode(data, "curta".toCharArray(), fast) }.isFailure)
    }
}
