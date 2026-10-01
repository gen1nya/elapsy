package app.elapsy.android

import android.content.res.Resources
import app.elapsy.core.CountLabel
import app.elapsy.core.CountType
import app.elapsy.core.DateFormatStyle
import app.elapsy.core.DayCount
import app.elapsy.core.Event
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.Locale
import kotlinx.datetime.LocalDate
import kotlinx.datetime.toJavaLocalDate

/** Text shared by the app screens and the home screen widget. */
object EventFormatter {
    /** The words under the number, e.g. "дня прошло". */
    fun caption(resources: Resources, count: DayCount): String = when (count.label) {
        CountLabel.ELAPSED -> resources.getQuantityString(R.plurals.caption_elapsed, count.days)
        CountLabel.REMAINING -> resources.getQuantityString(R.plurals.caption_remaining, count.days)
        CountLabel.AGO -> resources.getQuantityString(R.plurals.caption_ago, count.days)
        CountLabel.TODAY -> resources.getString(R.string.caption_today)
    }

    fun date(date: LocalDate, style: DateFormatStyle, locale: Locale = Locale.getDefault()): String {
        val formatStyle = when (style) {
            DateFormatStyle.SYSTEM -> FormatStyle.MEDIUM
            DateFormatStyle.NUMERIC -> FormatStyle.SHORT
            DateFormatStyle.TEXT -> FormatStyle.LONG
        }
        return DateTimeFormatter.ofLocalizedDate(formatStyle).withLocale(locale).format(date.toJavaLocalDate())
    }

    /** The secondary line of a card: "01.01.2026 — сегодня" for a running elapsed event, otherwise the date. */
    fun dateLine(resources: Resources, event: Event, today: LocalDate, style: DateFormatStyle): String {
        val date = date(event.date, style, resources.configuration.locales[0])
        return if (event.type == CountType.ELAPSED && event.date <= today) {
            resources.getString(R.string.date_range_until_today, date)
        } else {
            date
        }
    }
}
