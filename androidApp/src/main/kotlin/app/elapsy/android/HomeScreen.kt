package app.elapsy.android

import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import app.elapsy.core.AppData
import app.elapsy.core.Event
import app.elapsy.core.SortMode
import app.elapsy.core.sorted
import kotlinx.datetime.LocalDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun HomeScreen(
    data: AppData,
    today: LocalDate,
    busy: Boolean,
    snackbar: SnackbarHostState,
    onCreate: () -> Unit,
    onEdit: (String) -> Unit,
    onSettings: () -> Unit,
    onSort: (SortMode) -> Unit,
    onDuplicate: (String) -> Unit,
    onDelete: (String) -> Unit,
    onMove: (String, Int) -> Unit,
) {
    var sorting by rememberSaveable { mutableStateOf(false) }
    var deletingId by rememberSaveable { mutableStateOf<String?>(null) }
    val events = remember(data, today) { data.events.sorted(data.settings.sortMode, today, data.settings.firstDay) }
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.app_name), style = MaterialTheme.typography.titleLarge) },
                actions = {
                    IconButton(onClick = onCreate) { AppIcon(R.drawable.ic_add, stringResource(R.string.add_event)) }
                    Box {
                        IconButton(onClick = { sorting = true }) {
                            AppIcon(R.drawable.ic_sort, stringResource(R.string.sort_events))
                        }
                        DropdownMenu(expanded = sorting, onDismissRequest = { sorting = false }) {
                            SortMode.entries.forEach { mode ->
                                DropdownMenuItem(
                                    text = { Text(stringResource(mode.label())) },
                                    onClick = { onSort(mode); sorting = false },
                                    enabled = !busy,
                                    trailingIcon = {
                                        if (mode == data.settings.sortMode) AppIcon(R.drawable.ic_check)
                                    },
                                    modifier = Modifier.semantics { selected = mode == data.settings.sortMode },
                                )
                            }
                        }
                    }
                    IconButton(onClick = onSettings) {
                        AppIcon(R.drawable.ic_settings, stringResource(R.string.settings))
                    }
                },
            )
        },
        snackbarHost = { SnackbarHost(snackbar) },
    ) { insets ->
        Box(Modifier.fillMaxSize().padding(insets), contentAlignment = Alignment.TopCenter) {
            if (events.isEmpty()) {
                EmptyEvents(onCreate, Modifier.align(Alignment.Center))
            } else {
                LazyColumn(
                    modifier = Modifier.widthIn(max = 640.dp).fillMaxSize(),
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 24.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    itemsIndexed(events, key = { _, event -> event.id }) { index, event ->
                        HomeEventCard(
                            event = event,
                            data = data,
                            today = today,
                            busy = busy,
                            canMoveUp = index > 0,
                            canMoveDown = index < events.lastIndex,
                            onEdit = { onEdit(event.id) },
                            onDuplicate = { onDuplicate(event.id) },
                            onDelete = { deletingId = event.id },
                            onMove = { onMove(event.id, it) },
                        )
                    }
                }
            }
        }
    }
    data.events.firstOrNull { it.id == deletingId }?.let { event ->
        DeleteEventDialog(
            name = event.name,
            enabled = !busy,
            onDismiss = { deletingId = null },
            onDelete = { onDelete(event.id); deletingId = null },
        )
    }
}

@Composable
private fun EmptyEvents(onCreate: () -> Unit, modifier: Modifier = Modifier) {
    Column(
        modifier.widthIn(max = 400.dp).verticalScroll(rememberScrollState()).padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        Surface(shape = RoundedCornerShape(28.dp), color = MaterialTheme.colorScheme.primaryContainer) {
            Box(Modifier.size(88.dp), contentAlignment = Alignment.Center) { AppIcon(R.drawable.ic_calendar) }
        }
        Text(
            stringResource(R.string.empty_title),
            style = MaterialTheme.typography.headlineSmall,
            textAlign = TextAlign.Center,
        )
        Text(
            stringResource(R.string.empty_description),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
        Button(onClick = onCreate) { Text(stringResource(R.string.create_first_event)) }
    }
}

@Composable
private fun HomeEventCard(
    event: Event,
    data: AppData,
    today: LocalDate,
    busy: Boolean,
    canMoveUp: Boolean,
    canMoveDown: Boolean,
    onEdit: () -> Unit,
    onDuplicate: () -> Unit,
    onDelete: () -> Unit,
    onMove: (Int) -> Unit,
) {
    var actions by rememberSaveable { mutableStateOf(false) }
    Box {
        Box(
            Modifier.clip(RoundedCornerShape(28.dp)).combinedClickable(
                role = Role.Button,
                onClickLabel = stringResource(R.string.edit_event),
                onLongClickLabel = stringResource(R.string.event_actions),
                onClick = onEdit,
                onLongClick = { actions = true },
            )
        ) {
            EventPreview(event, data.settings, today, Modifier.fillMaxWidth())
        }
        DropdownMenu(expanded = actions, onDismissRequest = { actions = false }) {
            DropdownMenuItem(text = { Text(stringResource(R.string.edit_event)) }, onClick = { actions = false; onEdit() })
            DropdownMenuItem(
                text = { Text(stringResource(R.string.duplicate_event)) },
                enabled = !busy,
                onClick = { actions = false; onDuplicate() },
            )
            if (data.settings.sortMode == SortMode.CUSTOM) {
                DropdownMenuItem(
                    text = { Text(stringResource(R.string.move_up)) },
                    enabled = canMoveUp && !busy,
                    onClick = { actions = false; onMove(-1) },
                )
                DropdownMenuItem(
                    text = { Text(stringResource(R.string.move_down)) },
                    enabled = canMoveDown && !busy,
                    onClick = { actions = false; onMove(1) },
                )
            }
            DropdownMenuItem(
                text = { Text(stringResource(R.string.delete_event), color = MaterialTheme.colorScheme.error) },
                enabled = !busy,
                onClick = { actions = false; onDelete() },
            )
        }
    }
}
