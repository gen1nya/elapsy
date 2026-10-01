package app.elapsy.core

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlinx.datetime.LocalDate

class DayCountTest {
    private val today = LocalDate(2026, 10, 1)

    private fun event(type: CountType, date: LocalDate) = Event(
        id = "id",
        name = "name",
        type = type,
        date = date,
        colorArgb = 0xFF000000,
        createdAtEpochMs = 0,
        sortOrder = 0,
    )

    @Test
    fun elapsedCountsFromDayZeroByDefault() {
        val count = event(CountType.ELAPSED, LocalDate(2026, 1, 1)).dayCount(today, FirstDay.DAY_0)
        assertEquals(DayCount(273, CountLabel.ELAPSED), count)
    }

    @Test
    fun elapsedCountsStartDateAsDayOneWhenConfigured() {
        val count = event(CountType.ELAPSED, LocalDate(2026, 1, 1)).dayCount(today, FirstDay.DAY_1)
        assertEquals(DayCount(274, CountLabel.ELAPSED), count)
    }

    @Test
    fun elapsedStartingTodayIsZeroOrOne() {
        val event = event(CountType.ELAPSED, today)
        assertEquals(DayCount(0, CountLabel.ELAPSED), event.dayCount(today, FirstDay.DAY_0))
        assertEquals(DayCount(1, CountLabel.ELAPSED), event.dayCount(today, FirstDay.DAY_1))
    }

    @Test
    fun remainingCountsDaysUntilDate() {
        val count = event(CountType.REMAINING, LocalDate(2026, 12, 1)).dayCount(today, FirstDay.DAY_0)
        assertEquals(DayCount(61, CountLabel.REMAINING), count)
    }

    @Test
    fun agoCountsDaysSinceDate() {
        val count = event(CountType.AGO, LocalDate(2026, 9, 20)).dayCount(today, FirstDay.DAY_0)
        assertEquals(DayCount(11, CountLabel.AGO), count)
    }

    @Test
    fun firstDaySettingDoesNotAffectRemainingOrAgo() {
        assertEquals(
            DayCount(61, CountLabel.REMAINING),
            event(CountType.REMAINING, LocalDate(2026, 12, 1)).dayCount(today, FirstDay.DAY_1),
        )
        assertEquals(
            DayCount(11, CountLabel.AGO),
            event(CountType.AGO, LocalDate(2026, 9, 20)).dayCount(today, FirstDay.DAY_1),
        )
    }

    @Test
    fun remainingAndAgoOnTheDayItselfReadAsToday() {
        assertEquals(DayCount(0, CountLabel.TODAY), event(CountType.REMAINING, today).dayCount(today, FirstDay.DAY_0))
        assertEquals(DayCount(0, CountLabel.TODAY), event(CountType.AGO, today).dayCount(today, FirstDay.DAY_1))
    }

    @Test
    fun countdownPastItsDateReadsAsAgo() {
        val count = event(CountType.REMAINING, LocalDate(2026, 9, 28)).dayCount(today, FirstDay.DAY_1)
        assertEquals(DayCount(3, CountLabel.AGO), count)
    }

    @Test
    fun futureDatedElapsedAndAgoReadAsRemaining() {
        val future = LocalDate(2026, 10, 5)
        assertEquals(DayCount(4, CountLabel.REMAINING), event(CountType.ELAPSED, future).dayCount(today, FirstDay.DAY_1))
        assertEquals(DayCount(4, CountLabel.REMAINING), event(CountType.AGO, future).dayCount(today, FirstDay.DAY_0))
    }

    @Test
    fun leapDayIsCounted() {
        val count = event(CountType.ELAPSED, LocalDate(2024, 2, 28))
            .dayCount(LocalDate(2024, 3, 1), FirstDay.DAY_0)
        assertEquals(2, count.days)
    }
}
