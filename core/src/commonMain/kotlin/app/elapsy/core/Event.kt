package app.elapsy.core

import kotlinx.datetime.LocalDate
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class CountType {
    @SerialName("elapsed") ELAPSED,
    @SerialName("remaining") REMAINING,
    @SerialName("ago") AGO,
}

@Serializable
enum class BackgroundStyle {
    @SerialName("system") SYSTEM,
    @SerialName("solid") SOLID,
    @SerialName("gradient") GRADIENT,
}

@Serializable
data class Event(
    val id: String,
    val name: String,
    val type: CountType,
    /** The single date the event counts from or to, depending on [type]. */
    val date: LocalDate,
    /** 0xAARRGGBB. */
    val colorArgb: Long,
    val background: BackgroundStyle = BackgroundStyle.SOLID,
    /** An emoji, or null for no icon. */
    val icon: String? = null,
    val createdAtEpochMs: Long,
    val sortOrder: Int,
) {
    companion object {
        const val MAX_NAME_LENGTH = 40
    }
}
