package app.elapsy.android

import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.onLongClick
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import app.elapsy.core.AppData
import app.elapsy.core.Event
import app.elapsy.core.SortMode
import kotlinx.datetime.LocalDate
import sh.calvin.reorderable.ReorderableItem
import sh.calvin.reorderable.rememberReorderableLazyListState

@Composable
internal fun HomeEventList(
    events: List<Event>,
    data: AppData,
    today: LocalDate,
    busy: Boolean,
    onEdit: (String) -> Unit,
    onDuplicate: (String) -> Unit,
    onDelete: (String) -> Unit,
    onReorder: (List<String>) -> Unit,
) {
    var orderedEvents by remember(events) { mutableStateOf(events) }
    var actionsId by rememberSaveable { mutableStateOf<String?>(null) }
    val canDrag = data.settings.sortMode == SortMode.CUSTOM && !busy
    val listState = rememberLazyListState()
    val reorderState = rememberReorderableLazyListState(listState) { from, to ->
        if (canDrag) {
            val fromIndex = orderedEvents.indexOfFirst { it.id == from.key }
            val toIndex = orderedEvents.indexOfFirst { it.id == to.key }
            if (fromIndex >= 0 && toIndex >= 0) {
                orderedEvents = orderedEvents.toMutableList().apply { add(toIndex, removeAt(fromIndex)) }
            }
        }
    }
    // Restore the repository order if saving failed; successful saves also refresh events.
    LaunchedEffect(events, busy) {
        if (!busy) orderedEvents = events
    }
    LazyColumn(
        state = listState,
        modifier = Modifier.widthIn(max = 640.dp).fillMaxSize(),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        items(orderedEvents, key = { it.id }) { event ->
            ReorderableItem(reorderState, key = event.id) { _ ->
                val showActions = { actionsId = event.id }
                val gesture = rememberEventDragGesture(
                    onLongPress = showActions,
                    onDrop = {
                        val ids = orderedEvents.map { it.id }
                        if (ids != events.map { it.id }) onReorder(ids)
                    },
                    onCancel = { orderedEvents = events },
                )
                val editLabel = stringResource(R.string.edit_event)
                val actionsLabel = stringResource(R.string.event_actions)
                val interaction = if (canDrag) {
                    // Handle the long-press release before clickable can open the editor.
                    Modifier.clickable(role = Role.Button, onClickLabel = editLabel, onClick = { onEdit(event.id) })
                        .draggableHandle(dragGestureDetector = gesture)
                        .semantics { onLongClick(actionsLabel) { showActions(); true } }
                } else {
                    Modifier.combinedClickable(
                        role = Role.Button,
                        onClickLabel = editLabel,
                        onLongClickLabel = actionsLabel,
                        onClick = { onEdit(event.id) },
                        onLongClick = showActions,
                    )
                }
                Box {
                    Box(Modifier.clip(RoundedCornerShape(28.dp)).then(interaction)) {
                        EventPreview(event, data.settings, today, Modifier.fillMaxWidth())
                    }
                    EventActionsMenu(
                        expanded = actionsId == event.id,
                        busy = busy,
                        onDismiss = { actionsId = null },
                        onEdit = { onEdit(event.id) },
                        onDuplicate = { onDuplicate(event.id) },
                        onDelete = { onDelete(event.id) },
                    )
                }
            }
        }
    }
}

@Composable
private fun EventActionsMenu(
    expanded: Boolean,
    busy: Boolean,
    onDismiss: () -> Unit,
    onEdit: () -> Unit,
    onDuplicate: () -> Unit,
    onDelete: () -> Unit,
) {
    DropdownMenu(expanded = expanded, onDismissRequest = onDismiss) {
        DropdownMenuItem(text = { Text(stringResource(R.string.edit_event)) }, onClick = { onDismiss(); onEdit() })
        DropdownMenuItem(
            text = { Text(stringResource(R.string.duplicate_event)) },
            enabled = !busy,
            onClick = { onDismiss(); onDuplicate() },
        )
        DropdownMenuItem(
            text = { Text(stringResource(R.string.delete_event), color = MaterialTheme.colorScheme.error) },
            enabled = !busy,
            onClick = { onDismiss(); onDelete() },
        )
    }
}
