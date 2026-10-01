package app.elapsy.android

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.FilterChip
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import app.elapsy.core.BackgroundStyle
import app.elapsy.core.CountType
import app.elapsy.core.Event
import app.elapsy.core.Settings
import kotlinx.datetime.LocalDate

@Composable
internal fun EventEditorScreen(
    event: Event?,
    settings: Settings,
    today: LocalDate,
    busy: Boolean,
    snackbar: SnackbarHostState,
    onBack: () -> Unit,
    onSave: (Event) -> Unit,
    onDelete: () -> Unit,
) {
    var name by rememberSaveable { mutableStateOf(event?.name.orEmpty()) }
    var type by rememberSaveable { mutableStateOf(event?.type ?: CountType.ELAPSED) }
    var dateText by rememberSaveable { mutableStateOf((event?.date ?: today).toString()) }
    var color by rememberSaveable { mutableLongStateOf(event?.colorArgb ?: eventPalette.first()) }
    var background by rememberSaveable { mutableStateOf(event?.background ?: BackgroundStyle.SOLID) }
    var icon by rememberSaveable { mutableStateOf(event?.icon) }
    var shape by rememberSaveable { mutableStateOf(PreviewShape.RECTANGLE) }
    var datePicker by rememberSaveable { mutableStateOf(false) }
    var emojiPicker by rememberSaveable { mutableStateOf(false) }
    var deleting by rememberSaveable { mutableStateOf(false) }
    val date = LocalDate.parse(dateText)
    val draft = Event(
        id = event?.id.orEmpty(), name = name, type = type, date = date,
        colorArgb = color, background = background, icon = icon,
        createdAtEpochMs = event?.createdAtEpochMs ?: 0L, sortOrder = event?.sortOrder ?: 0,
    )
    val initial = remember(event) {
        event ?: Event(
            id = "", name = "", type = CountType.ELAPSED, date = today,
            colorArgb = eventPalette.first(), background = BackgroundStyle.SOLID, icon = null,
            createdAtEpochMs = 0L, sortOrder = 0,
        )
    }
    val hasChanges = draft.copy(name = name.trim()) != initial
    var discarding by rememberSaveable { mutableStateOf(false) }
    // Leaving with unsaved edits must be a deliberate choice, whichever way the user leaves.
    val close = { if (hasChanges) discarding = true else onBack() }
    BackHandler(enabled = hasChanges) { discarding = true }
    val resources = LocalResources.current
    val focus = LocalFocusManager.current
    Scaffold(
        modifier = Modifier.imePadding(),
        topBar = {
            EditorTopBar(
                title = stringResource(if (event == null) R.string.new_event else R.string.edit_event_title),
                saveLabel = stringResource(if (event == null) R.string.create else R.string.save),
                saveEnabled = name.isNotBlank() && hasChanges && !busy,
                onClose = close,
                onSave = { focus.clearFocus(); onSave(draft) },
            )
        },
        snackbarHost = { SnackbarHost(snackbar) },
    ) { insets ->
        Box(Modifier.fillMaxSize().padding(insets).consumeWindowInsets(insets), contentAlignment = Alignment.TopCenter) {
            // The preview stays pinned so every change below is visible as it is made.
            Column(Modifier.widthIn(max = 640.dp).fillMaxSize()) {
                Column(
                    Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    EventPreview(
                        draft, settings, today,
                        modifier = if (shape == PreviewShape.SQUARE) {
                            Modifier.widthIn(max = 170.dp).fillMaxWidth().aspectRatio(1f)
                        } else {
                            Modifier.fillMaxWidth()
                        },
                        shape = shape,
                    )
                    Text(stringResource(R.string.preview_shape), style = MaterialTheme.typography.labelLarge)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilterChip(
                            selected = shape == PreviewShape.SQUARE,
                            onClick = { shape = PreviewShape.SQUARE },
                            label = { Text(stringResource(R.string.shape_square)) },
                        )
                        FilterChip(
                            selected = shape == PreviewShape.RECTANGLE,
                            onClick = { shape = PreviewShape.RECTANGLE },
                            label = { Text(stringResource(R.string.shape_rectangle)) },
                        )
                    }
                }
                Column(
                    modifier = Modifier.fillMaxWidth().weight(1f).verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it.take(Event.MAX_NAME_LENGTH) },
                        label = { Text(stringResource(R.string.event_name)) },
                        placeholder = { Text(stringResource(R.string.event_name_example)) },
                        supportingText = { Text(stringResource(R.string.name_length, name.length, Event.MAX_NAME_LENGTH)) },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences, imeAction = ImeAction.Done),
                        keyboardActions = KeyboardActions(onDone = { focus.clearFocus() }),
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                    )
                    ChoiceField(
                        title = stringResource(R.string.count_type), value = type,
                        options = CountType.entries, label = { it.label() },
                        onSelected = { type = it },
                    )
                    ListItem(
                        headlineContent = { Text(stringResource(type.dateLabel())) },
                        supportingContent = {
                            Text(EventFormatter.date(date, settings.dateFormat, resources.configuration.locales[0]))
                        },
                        trailingContent = { AppIcon(R.drawable.ic_calendar) },
                        modifier = Modifier.clickable(role = Role.Button) { focus.clearFocus(); datePicker = true },
                    )
                    ColorPalette(color) { color = it }
                    ChoiceField(
                        title = stringResource(R.string.background), value = background,
                        options = BackgroundStyle.entries, label = { it.label() },
                        onSelected = { background = it },
                    )
                    ListItem(
                        headlineContent = { Text(stringResource(R.string.icon)) },
                        supportingContent = { Text(icon ?: stringResource(R.string.no_icon)) },
                        trailingContent = { AppIcon(R.drawable.ic_expand) },
                        modifier = Modifier.clickable(role = Role.Button) { focus.clearFocus(); emojiPicker = true },
                    )
                    if (event != null) {
                        TextButton(
                            onClick = { deleting = true }, enabled = !busy,
                            modifier = Modifier.align(Alignment.CenterHorizontally),
                        ) { Text(stringResource(R.string.delete_event), color = MaterialTheme.colorScheme.error) }
                    }
                    Spacer(Modifier.height(16.dp))
                }
            }
        }
    }
    if (datePicker) EventDatePicker(date, { dateText = it.toString() }, { datePicker = false })
    if (emojiPicker) EmojiPicker(icon, { icon = it }, { emojiPicker = false })
    if (discarding) {
        DiscardChangesDialog(onDismiss = { discarding = false }, onDiscard = { discarding = false; onBack() })
    }
    if (deleting) {
        DeleteEventDialog(
            name = event?.name.orEmpty(), enabled = !busy, onDismiss = { deleting = false },
            onDelete = { deleting = false; onDelete() },
        )
    }
}
