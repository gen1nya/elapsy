package app.elapsy.android

import app.elapsy.core.BackgroundStyle
import app.elapsy.core.CountType
import app.elapsy.core.DateFormatStyle
import app.elapsy.core.FirstDay
import app.elapsy.core.SortMode
import app.elapsy.core.ThemeMode

internal fun SortMode.label(): Int = when (this) {
    SortMode.CUSTOM -> R.string.sort_custom
    SortMode.NEWEST -> R.string.sort_newest
    SortMode.OLDEST -> R.string.sort_oldest
    SortMode.NAME -> R.string.sort_name
    SortMode.DAYS -> R.string.sort_days
    SortMode.NEAREST -> R.string.sort_nearest
}

internal fun CountType.label(): Int = when (this) {
    CountType.ELAPSED -> R.string.type_elapsed
    CountType.REMAINING -> R.string.type_remaining
    CountType.AGO -> R.string.type_ago
}

internal fun CountType.dateLabel(): Int = when (this) {
    CountType.ELAPSED -> R.string.start_date
    CountType.REMAINING -> R.string.end_date
    CountType.AGO -> R.string.event_date
}

internal fun BackgroundStyle.label(): Int = when (this) {
    BackgroundStyle.SYSTEM -> R.string.background_system
    BackgroundStyle.SOLID -> R.string.background_solid
    BackgroundStyle.GRADIENT -> R.string.background_gradient
}

internal fun ThemeMode.label(): Int = when (this) {
    ThemeMode.SYSTEM -> R.string.theme_system
    ThemeMode.LIGHT -> R.string.theme_light
    ThemeMode.DARK -> R.string.theme_dark
}

internal fun FirstDay.label(): Int = when (this) {
    FirstDay.DAY_0 -> R.string.day_zero
    FirstDay.DAY_1 -> R.string.day_one
}

internal fun DateFormatStyle.label(): Int = when (this) {
    DateFormatStyle.SYSTEM -> R.string.date_format_system
    DateFormatStyle.NUMERIC -> R.string.date_format_numeric
    DateFormatStyle.TEXT -> R.string.date_format_text
}
