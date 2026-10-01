package app.elapsy.core

import kotlin.time.Clock
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/** Where the serialized [AppData] document lives. Platform-specific. */
interface DataStorage {
    fun read(): String?
    fun write(text: String)
}

/**
 * Single source of truth for events and settings. Every change is persisted immediately and
 * reported through [onChanged], which platforms use to refresh their widgets.
 */
class EventRepository(
    private val storage: DataStorage,
    private val onChanged: () -> Unit = {},
    private val newId: () -> String = ::randomId,
    private val nowEpochMs: () -> Long = { Clock.System.now().toEpochMilliseconds() },
) {
    private val _data = MutableStateFlow(storage.read()?.let(AppDataCodec::decodeOrNull) ?: AppData())
    val data: StateFlow<AppData> = _data.asStateFlow()

    fun event(id: String): Event? = _data.value.events.firstOrNull { it.id == id }

    /** Adds a new event at the end of the custom order and returns it. */
    fun create(
        name: String,
        type: CountType,
        date: kotlinx.datetime.LocalDate,
        colorArgb: Long,
        background: BackgroundStyle,
        icon: String?,
    ): Event {
        val event = Event(
            id = newId(),
            name = name,
            type = type,
            date = date,
            colorArgb = colorArgb,
            background = background,
            icon = icon,
            createdAtEpochMs = nowEpochMs(),
            sortOrder = nextSortOrder(),
        )
        mutate { it.copy(events = it.events + event) }
        return event
    }

    fun update(event: Event) {
        mutate { data -> data.copy(events = data.events.map { if (it.id == event.id) event else it }) }
    }

    fun delete(id: String) {
        mutate { data -> data.copy(events = data.events.filterNot { it.id == id }) }
    }

    fun duplicate(id: String): Event? {
        val source = event(id) ?: return null
        val copy = source.copy(id = newId(), createdAtEpochMs = nowEpochMs(), sortOrder = nextSortOrder())
        mutate { it.copy(events = it.events + copy) }
        return copy
    }

    /** Rewrites the custom order to match [orderedIds]; events not listed keep their relative order after them. */
    fun reorder(orderedIds: List<String>) {
        val position = orderedIds.withIndex().associate { (index, id) -> id to index }
        mutate { data ->
            val reordered = data.events
                .sortedWith(compareBy({ position[it.id] ?: Int.MAX_VALUE }, { it.sortOrder }))
                .mapIndexed { index, event -> event.copy(sortOrder = index) }
            data.copy(events = reordered)
        }
    }

    fun updateSettings(transform: (Settings) -> Settings) {
        mutate { it.copy(settings = transform(it.settings)) }
    }

    fun exportJson(): String = AppDataCodec.encode(_data.value)

    /**
     * Merges events from an exported document: an imported event replaces the local one with the
     * same id, the rest are appended. Local settings are kept. Returns the number of imported
     * events, or null if [text] is not a valid export.
     */
    fun importJson(text: String): Int? {
        val imported = AppDataCodec.decodeOrNull(text) ?: return null
        val importedIds = imported.events.map { it.id }.toSet()
        mutate { data ->
            var order = (data.events.maxOfOrNull { it.sortOrder } ?: -1) + 1
            val existingById = data.events.associateBy { it.id }
            val merged = data.events.filterNot { it.id in importedIds } + imported.events.map { event ->
                event.copy(sortOrder = existingById[event.id]?.sortOrder ?: order++)
            }
            data.copy(events = merged)
        }
        return imported.events.size
    }

    private fun nextSortOrder(): Int = (_data.value.events.maxOfOrNull { it.sortOrder } ?: -1) + 1

    private fun mutate(transform: (AppData) -> AppData) {
        val updated = transform(_data.value)
        if (updated == _data.value) return
        storage.write(AppDataCodec.encode(updated))
        _data.value = updated
        onChanged()
    }
}

@OptIn(ExperimentalUuidApi::class)
private fun randomId(): String = Uuid.random().toString()
