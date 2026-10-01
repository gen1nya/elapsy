package app.elapsy.android

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import app.elapsy.core.AppData
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
    onReorder: (List<String>) -> Unit,
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
                HomeEventList(
                    events = events,
                    data = data,
                    today = today,
                    busy = busy,
                    onEdit = onEdit,
                    onDuplicate = onDuplicate,
                    onDelete = { deletingId = it },
                    onReorder = onReorder,
                )
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
