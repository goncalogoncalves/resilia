package com.goncalogoncalves.resilia.core.domain.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class KitAndInventoryTest {

    private val t = LocalizedText.of("pt", "en")

    private val catalog = KitCatalog(
        minimumDays = 3,
        minimumDaysSourceIds = listOf("dgs"),
        dayOptions = listOf(3, 7, 14),
        reviewIntervalMonths = 6,
        reviewSourceIds = listOf("cvp"),
        items = listOf(
            KitItem("home.water", KitList.HOME, name = t,
                quantity = KitQuantity(KitQuantityKind.PER_PERSON_PER_DAY, 2.0, KitUnit.LITRE)),
            KitItem("home.food", KitList.HOME, name = t, quantity = KitQuantity(KitQuantityKind.FOR_DURATION)),
            KitItem("home.baby", KitList.HOME, KitCondition.BABY, name = t),
            KitItem("home.pets", KitList.HOME, KitCondition.PETS, name = t),
            KitItem("bag.cash", KitList.BAG, name = t),
        ),
    )

    @Test
    fun `water is 2 litres per person per day`() {
        val lines = KitCalculator.calculate(catalog, Household(people = 4, days = 3), KitList.HOME)
        assertEquals(24.0, lines.first { it.item.id == "home.water" }.total!!, 0.0)
    }

    @Test
    fun `days below the official minimum are raised to it`() {
        val lines = KitCalculator.calculate(catalog, Household(people = 1, days = 1), KitList.HOME)
        assertEquals(6.0, lines.first { it.item.id == "home.water" }.total!!, 0.0)
        assertEquals(3, lines.first().days)
    }

    @Test
    fun `items without a per-person amount have no total`() {
        val lines = KitCalculator.calculate(catalog, Household(people = 2, days = 7), KitList.HOME)
        val food = lines.first { it.item.id == "home.food" }
        assertNull(food.total)
        assertEquals(2, food.people)
        assertEquals(7, food.days)
    }

    @Test
    fun `conditional items appear only when they apply`() {
        val plain = KitCalculator.calculate(catalog, Household(), KitList.HOME).map { it.item.id }
        assertFalse("home.baby" in plain)
        assertFalse("home.pets" in plain)
        val withBaby = KitCalculator.calculate(catalog, Household(hasBaby = true), KitList.HOME).map { it.item.id }
        assertTrue("home.baby" in withBaby)
        assertFalse("home.pets" in withBaby)
    }

    @Test
    fun `lists are separate`() {
        val bag = KitCalculator.calculate(catalog, Household(), KitList.BAG).map { it.item.id }
        assertEquals(listOf("bag.cash"), bag)
    }

    private val today = LocalDate.of(2026, 11, 20)

    @Test
    fun `expiry status boundaries`() {
        assertEquals(ExpiryStatus.NoDate, Expiry.status(null, today))
        assertEquals(ExpiryStatus.Today, Expiry.status(today, today))
        assertEquals(ExpiryStatus.Expired(1), Expiry.status(today.minusDays(1), today))
        assertEquals(ExpiryStatus.Soon(30), Expiry.status(today.plusDays(30), today))
        assertEquals(ExpiryStatus.Ok(31), Expiry.status(today.plusDays(31), today))
    }

    private fun item(expires: LocalDate?, soonFor: LocalDate? = null, expiredFor: LocalDate? = null) =
        InventoryItem(name = "x", expiresOn = expires, addedOn = today, notifiedSoonFor = soonFor, notifiedExpiredFor = expiredFor)

    @Test
    fun `reminders are sent once per expiry date`() {
        val soon = item(today.plusDays(10))
        val expired = item(today)
        val due = Expiry.dueReminders(listOf(soon, expired, item(null), item(today.plusDays(90))), today)
        assertEquals(listOf(soon), due.soon)
        assertEquals(listOf(expired), due.expired)

        val alreadySent = Expiry.dueReminders(
            listOf(item(today.plusDays(10), soonFor = today.plusDays(10)), item(today, expiredFor = today)),
            today,
        )
        assertTrue(alreadySent.isEmpty)
    }

    @Test
    fun `changing the date re-arms the reminder`() {
        val due = Expiry.dueReminders(listOf(item(today.plusDays(5), soonFor = today.plusDays(2))), today)
        assertEquals(1, due.soon.size)
    }

    @Test
    fun `review is six months after the last review or the first item`() {
        assertNull(KitReview.nextReview(null, null, 6))
        assertEquals(LocalDate.of(2027, 5, 20), KitReview.nextReview(null, today, 6))
        assertEquals(LocalDate.of(2027, 1, 1), KitReview.nextReview(LocalDate.of(2026, 7, 1), today, 6))
        assertFalse(KitReview.isDue(LocalDate.of(2027, 5, 20), today))
        assertTrue(KitReview.isDue(today, today))
        assertFalse(KitReview.isDue(null, today))
    }
}
