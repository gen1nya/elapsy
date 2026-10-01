package app.elapsy.core

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlinx.datetime.LocalDate

class SortingTest {
    private val today = LocalDate(2026, 10, 1)

    private fun event(name: String, date: LocalDate, createdAt: Long, sortOrder: Int, type: CountType = CountType.AGO) =
        Event(
            id = name,
            name = name,
            type = type,
            date = date,
            colorArgb = 0xFF000000,
            createdAtEpochMs = createdAt,
            sortOrder = sortOrder,
        )

    private val sober = event("не пью", LocalDate(2026, 1, 1), createdAt = 300, sortOrder = 1, type = CountType.ELAPSED)
    private val vacation = event("Отпуск", LocalDate(2026, 12, 1), createdAt = 100, sortOrder = 2, type = CountType.REMAINING)
    private val birthday = event("День рождения", LocalDate(2026, 10, 3), createdAt = 200, sortOrder = 0, type = CountType.REMAINING)
    private val release = event("Релиз", LocalDate(2026, 9, 20), createdAt = 400, sortOrder = 3)
    private val events = listOf(sober, vacation, birthday, release)

    private fun names(mode: SortMode) = events.sorted(mode, today, FirstDay.DAY_0).map { it.name }

    @Test
    fun customUsesSortOrder() {
        assertEquals(listOf("День рождения", "не пью", "Отпуск", "Релиз"), names(SortMode.CUSTOM))
    }

    @Test
    fun newestAndOldestUseCreationTime() {
        assertEquals(listOf("Релиз", "не пью", "День рождения", "Отпуск"), names(SortMode.NEWEST))
        assertEquals(listOf("Отпуск", "День рождения", "не пью", "Релиз"), names(SortMode.OLDEST))
    }

    @Test
    fun nameIgnoresCase() {
        assertEquals(listOf("День рождения", "не пью", "Отпуск", "Релиз"), names(SortMode.NAME))
    }

    @Test
    fun daysPutsLargestCountFirst() {
        assertEquals(listOf("не пью", "Отпуск", "Релиз", "День рождения"), names(SortMode.DAYS))
    }

    @Test
    fun nearestPutsUpcomingFirstThenMostRecentPast() {
        assertEquals(listOf("День рождения", "Отпуск", "Релиз", "не пью"), names(SortMode.NEAREST))
    }
}
