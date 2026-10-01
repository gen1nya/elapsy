package app.elapsy.core

import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json

object AppDataCodec {
    private val json = Json {
        ignoreUnknownKeys = true
        coerceInputValues = true
        encodeDefaults = true
        prettyPrint = true
    }

    fun encode(data: AppData): String = json.encodeToString(AppData.serializer(), data)

    fun decodeOrNull(text: String): AppData? =
        try {
            json.decodeFromString(AppData.serializer(), text)
        } catch (_: SerializationException) {
            null
        } catch (_: IllegalArgumentException) {
            null
        }
}
