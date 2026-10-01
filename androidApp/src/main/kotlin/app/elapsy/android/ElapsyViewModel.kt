package app.elapsy.android

import android.content.ContentResolver
import android.net.Uri
import androidx.annotation.StringRes
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.elapsy.core.Event
import app.elapsy.core.EventRepository
import app.elapsy.core.Settings
import app.elapsy.core.currentDate
import java.io.IOException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

internal data class UiMessage(@param:StringRes val text: Int, val count: Int? = null)

internal class ElapsyViewModel(private val repository: EventRepository) : ViewModel() {
    val data = repository.data
    var today by mutableStateOf(currentDate())
        private set
    var busy by mutableStateOf(false)
        private set
    private val messageChannel = Channel<UiMessage>(Channel.BUFFERED)
    val messages = messageChannel.receiveAsFlow()

    fun refreshToday() {
        today = currentDate()
    }

    fun save(event: Event, creating: Boolean, onSaved: () -> Unit) = perform(onSuccess = onSaved) {
        if (creating) {
            repository.create(
                name = event.name.trim().take(Event.MAX_NAME_LENGTH),
                type = event.type,
                date = event.date,
                colorArgb = event.colorArgb,
                background = event.background,
                icon = event.icon,
            )
        } else {
            repository.update(event.copy(name = event.name.trim().take(Event.MAX_NAME_LENGTH)))
        }
    }

    fun delete(id: String, onDeleted: () -> Unit = {}) = perform(onSuccess = onDeleted) {
        repository.delete(id)
    }

    fun duplicate(id: String) = perform {
        repository.duplicate(id)
    }

    fun move(id: String, offset: Int) = perform {
        val ids = repository.data.value.events.sortedBy { it.sortOrder }.map { it.id }.toMutableList()
        val index = ids.indexOf(id)
        val destination = index + offset
        if (index in ids.indices && destination in ids.indices) {
            ids.add(destination, ids.removeAt(index))
            repository.reorder(ids)
        }
    }

    fun updateSettings(transform: (Settings) -> Settings) = perform {
        repository.updateSettings(transform)
    }

    fun exportTo(resolver: ContentResolver, uri: Uri) = perform(R.string.export_failed) {
        val text = repository.exportJson()
        val stream = resolver.openOutputStream(uri, "wt") ?: throw IOException()
        stream.bufferedWriter(Charsets.UTF_8).use { it.write(text) }
        messageChannel.send(UiMessage(R.string.export_success))
    }

    fun importFrom(resolver: ContentResolver, uri: Uri) = perform(R.string.import_failed) {
        val stream = resolver.openInputStream(uri) ?: throw IOException()
        val text = stream.bufferedReader(Charsets.UTF_8).use { it.readText() }
        val count = repository.importJson(text)
        messageChannel.send(
            if (count == null) UiMessage(R.string.import_invalid)
            else UiMessage(R.string.import_success, count)
        )
    }

    fun documentPickerUnavailable() {
        messageChannel.trySend(UiMessage(R.string.document_picker_unavailable))
    }

    private fun perform(
        @StringRes error: Int = R.string.save_failed,
        onSuccess: () -> Unit = {},
        action: suspend () -> Unit,
    ) {
        if (busy) return
        busy = true
        viewModelScope.launch {
            try {
                withContext(Dispatchers.IO) { action() }
                onSuccess()
            } catch (e: CancellationException) {
                throw e
            } catch (_: Exception) {
                messageChannel.send(UiMessage(error))
            } finally {
                busy = false
            }
        }
    }
}
