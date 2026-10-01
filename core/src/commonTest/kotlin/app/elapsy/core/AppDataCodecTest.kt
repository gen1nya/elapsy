package app.elapsy.core

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlinx.datetime.LocalDate

class AppDataCodecTest {
    private val data = AppData(
        settings = Settings(firstDay = FirstDay.DAY_1, dateFormat = DateFormatStyle.TEXT),
        events = listOf(
            Event(
                id = "e1",
                name = "Не пью",
                type = CountType.ELAPSED,
                date = LocalDate(2026, 1, 1),
                colorArgb = 0xFFFF6B35,
                background = BackgroundStyle.GRADIENT,
                icon = "🍺",
                createdAtEpochMs = 1_790_000_000_000,
                sortOrder = 0,
            ),
        ),
    )

    @Test
    fun roundTrips() {
        assertEquals(data, AppDataCodec.decodeOrNull(AppDataCodec.encode(data)))
    }

    /** The native widgets parse this document themselves, so its shape is a contract. */
    @Test
    fun readsTheDocumentedFormat() {
        val text = """
            {
              "schemaVersion": 1,
              "settings": {"firstDay": "day1", "theme": "system", "dateFormat": "text", "sortMode": "custom"},
              "events": [{
                "id": "e1",
                "name": "Не пью",
                "type": "elapsed",
                "date": "2026-01-01",
                "colorArgb": 4294929205,
                "background": "gradient",
                "icon": "🍺",
                "createdAtEpochMs": 1790000000000,
                "sortOrder": 0
              }]
            }
        """.trimIndent()
        assertEquals(data, AppDataCodec.decodeOrNull(text))
    }

    @Test
    fun toleratesUnknownKeysAndValuesFromNewerVersions() {
        val text = """
            {
              "schemaVersion": 2,
              "somethingNew": true,
              "events": [{
                "id": "e1", "name": "n", "type": "ago", "date": "2026-09-20", "colorArgb": 4278190080,
                "background": "hologram", "createdAtEpochMs": 1, "sortOrder": 0, "extra": [1, 2]
              }]
            }
        """.trimIndent()
        val event = AppDataCodec.decodeOrNull(text)?.events?.single()
        assertEquals(BackgroundStyle.SOLID, event?.background)
        assertNull(event?.icon)
    }

    @Test
    fun rejectsInvalidDocuments() {
        assertNull(AppDataCodec.decodeOrNull(""))
        assertNull(AppDataCodec.decodeOrNull("[]"))
        assertNull(AppDataCodec.decodeOrNull("""{"events": [{"id": "e1"}]}"""))
    }
}
