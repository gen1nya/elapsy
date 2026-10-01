package app.elapsy.core

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlinx.datetime.LocalDate

class EventRepositoryTest {
    private class MemoryStorage(var text: String? = null) : DataStorage {
        override fun read(): String? = text
        override fun write(text: String) {
            this.text = text
        }
    }

    private val storage = MemoryStorage()
    private var changes = 0
    private var nextId = 0
    private var clock = 1000L

    private fun repository(storage: DataStorage = this.storage) = EventRepository(
        storage = storage,
        onChanged = { changes++ },
        newId = { "id-${nextId++}" },
        nowEpochMs = { clock++ },
    )

    private fun EventRepository.add(name: String) = create(
        name = name,
        type = CountType.ELAPSED,
        date = LocalDate(2026, 1, 1),
        colorArgb = 0xFFFF6B35,
        background = BackgroundStyle.SOLID,
        icon = "🍺",
    )

    @Test
    fun startsEmptyWhenNothingIsStored() {
        assertEquals(AppData(), repository().data.value)
    }

    @Test
    fun startsEmptyWhenStoredDocumentIsCorrupt() {
        assertEquals(AppData(), repository(MemoryStorage("{not json")).data.value)
    }

    @Test
    fun createdEventsSurviveReload() {
        val repository = repository()
        val first = repository.add("Не пью")
        val second = repository.add("Отпуск")

        assertEquals(listOf(0, 1), listOf(first.sortOrder, second.sortOrder))
        assertEquals(2, changes)
        assertEquals(listOf(first, second), repository().data.value.events)
    }

    @Test
    fun updateReplacesEventAndIgnoresNoOps() {
        val repository = repository()
        val event = repository.add("Не пью")
        changes = 0

        repository.update(event)
        assertEquals(0, changes)

        repository.update(event.copy(name = "Не курю"))
        assertEquals(1, changes)
        assertEquals("Не курю", repository.event(event.id)?.name)
    }

    @Test
    fun deleteRemovesEvent() {
        val repository = repository()
        val event = repository.add("Не пью")
        repository.delete(event.id)
        assertNull(repository.event(event.id))
    }

    @Test
    fun duplicateCopiesEverythingButIdentity() {
        val repository = repository()
        val source = repository.add("Не пью")
        val copy = assertNotNull(repository.duplicate(source.id))

        assertTrue(copy.id != source.id)
        assertEquals(source.sortOrder + 1, copy.sortOrder)
        assertEquals(source.copy(id = copy.id, createdAtEpochMs = copy.createdAtEpochMs, sortOrder = copy.sortOrder), copy)
        assertNull(repository.duplicate("missing"))
    }

    @Test
    fun reorderRewritesCustomOrder() {
        val repository = repository()
        val a = repository.add("a")
        val b = repository.add("b")
        val c = repository.add("c")

        repository.reorder(listOf(c.id, a.id))

        val order = repository.data.value.events.sortedBy { it.sortOrder }.map { it.name }
        assertEquals(listOf("c", "a", "b"), order)
    }

    @Test
    fun settingsArePersisted() {
        repository().updateSettings { it.copy(firstDay = FirstDay.DAY_1, sortMode = SortMode.NAME) }
        assertEquals(Settings(firstDay = FirstDay.DAY_1, sortMode = SortMode.NAME), repository().data.value.settings)
    }

    @Test
    fun importMergesByIdAndKeepsLocalSettings() {
        val source = repository(MemoryStorage())
        val shared = source.add("Не пью")
        source.add("Отпуск")
        source.updateSettings { it.copy(theme = ThemeMode.DARK) }
        val exported = source.exportJson()

        val target = repository()
        target.add("Локальное")
        // An older copy of an exported event: the import must overwrite it in place.
        target.importJson(AppDataCodec.encode(AppData(events = listOf(shared.copy(name = "Устаревшее")))))
        val staleOrder = target.event(shared.id)?.sortOrder

        assertEquals(2, target.importJson(exported))

        val events = target.data.value.events
        assertEquals(setOf("Локальное", "Не пью", "Отпуск"), events.map { it.name }.toSet())
        assertEquals(staleOrder, target.event(shared.id)?.sortOrder)
        assertEquals(events.size, events.map { it.sortOrder }.toSet().size)
        assertEquals(ThemeMode.SYSTEM, target.data.value.settings.theme)
    }

    @Test
    fun importRejectsGarbage() {
        val repository = repository()
        repository.add("Не пью")
        assertNull(repository.importJson("definitely not json"))
        assertEquals(1, repository.data.value.events.size)
    }
}
