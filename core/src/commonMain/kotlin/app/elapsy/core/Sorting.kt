package app.elapsy.core

import kotlin.math.abs
import kotlinx.datetime.LocalDate
import kotlinx.datetime.daysUntil

fun List<Event>.sorted(mode: SortMode, today: LocalDate, firstDay: FirstDay): List<Event> =
    when (mode) {
        SortMode.CUSTOM -> sortedBy { it.sortOrder }
        SortMode.NEWEST -> sortedByDescending { it.createdAtEpochMs }
        SortMode.OLDEST -> sortedBy { it.createdAtEpochMs }
        SortMode.NAME -> sortedBy { it.name.lowercase() }
        SortMode.DAYS -> sortedByDescending { it.dayCount(today, firstDay).days }
        // Upcoming dates first (soonest on top), then past ones (most recent on top).
        SortMode.NEAREST -> sortedWith(
            compareBy<Event> { it.date < today }.thenBy { abs(it.date.daysUntil(today)) }
        )
    }
