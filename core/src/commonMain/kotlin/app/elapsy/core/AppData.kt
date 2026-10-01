package app.elapsy.core

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class FirstDay {
    @SerialName("day0") DAY_0,
    @SerialName("day1") DAY_1,
}

@Serializable
enum class ThemeMode {
    @SerialName("system") SYSTEM,
    @SerialName("light") LIGHT,
    @SerialName("dark") DARK,
}

@Serializable
enum class DateFormatStyle {
    @SerialName("system") SYSTEM,
    @SerialName("numeric") NUMERIC,
    @SerialName("text") TEXT,
}

@Serializable
enum class SortMode {
    @SerialName("custom") CUSTOM,
    @SerialName("newest") NEWEST,
    @SerialName("oldest") OLDEST,
    @SerialName("name") NAME,
    @SerialName("days") DAYS,
    @SerialName("nearest") NEAREST,
}

@Serializable
data class Settings(
    val firstDay: FirstDay = FirstDay.DAY_0,
    val theme: ThemeMode = ThemeMode.SYSTEM,
    val dateFormat: DateFormatStyle = DateFormatStyle.SYSTEM,
    val sortMode: SortMode = SortMode.CUSTOM,
)

/**
 * Everything the app persists. Serialized as one JSON document that is both the on-disk store
 * (read directly by the native widgets) and the export/import format.
 */
@Serializable
data class AppData(
    val schemaVersion: Int = CURRENT_SCHEMA_VERSION,
    val settings: Settings = Settings(),
    val events: List<Event> = emptyList(),
) {
    companion object {
        const val CURRENT_SCHEMA_VERSION = 1
    }
}
