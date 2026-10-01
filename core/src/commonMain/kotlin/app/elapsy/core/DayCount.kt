package app.elapsy.core

import kotlin.time.Clock
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.daysUntil
import kotlinx.datetime.todayIn

/** Which caption goes under the number. Not always the event's own type: see [dayCount]. */
enum class CountLabel { ELAPSED, REMAINING, AGO, TODAY }

data class DayCount(val days: Int, val label: CountLabel)

/**
 * Counts whole calendar days between the event date and [today].
 *
 * A date on the "wrong" side of today flips the caption instead of going negative: a countdown
 * whose date has passed reads as "ago", and an elapsed/ago event dated in the future reads as
 * "remaining". [firstDay] only shifts ELAPSED counts.
 */
fun Event.dayCount(today: LocalDate, firstDay: FirstDay): DayCount {
    val daysSince = date.daysUntil(today)
    return when {
        daysSince < 0 -> DayCount(-daysSince, CountLabel.REMAINING)
        type == CountType.ELAPSED ->
            DayCount(daysSince + if (firstDay == FirstDay.DAY_1) 1 else 0, CountLabel.ELAPSED)
        daysSince == 0 -> DayCount(0, CountLabel.TODAY)
        else -> DayCount(daysSince, CountLabel.AGO)
    }
}

fun currentDate(timeZone: TimeZone = TimeZone.currentSystemDefault()): LocalDate =
    Clock.System.todayIn(timeZone)
