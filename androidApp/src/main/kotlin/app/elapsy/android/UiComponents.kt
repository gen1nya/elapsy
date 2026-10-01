package app.elapsy.android

import androidx.annotation.DrawableRes
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp

@Composable
internal fun AppIcon(@DrawableRes icon: Int, description: String? = null, modifier: Modifier = Modifier) {
    Icon(painterResource(icon), description, modifier.size(24.dp))
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun BackTopBar(title: String, onBack: () -> Unit) {
    TopAppBar(
        title = { Text(title, style = MaterialTheme.typography.titleLarge) },
        navigationIcon = {
            IconButton(onClick = onBack) { AppIcon(R.drawable.ic_back, stringResource(R.string.back)) }
        },
    )
}

/** Top bar of a screen that edits something: closing and saving are both explicit and always visible. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun EditorTopBar(title: String, saveLabel: String, saveEnabled: Boolean, onClose: () -> Unit, onSave: () -> Unit) {
    TopAppBar(
        title = { Text(title, style = MaterialTheme.typography.titleLarge) },
        navigationIcon = {
            IconButton(onClick = onClose) { AppIcon(R.drawable.ic_close, stringResource(R.string.close)) }
        },
        actions = {
            TextButton(onClick = onSave, enabled = saveEnabled) { Text(saveLabel) }
        },
    )
}

@Composable
internal fun DiscardChangesDialog(onDismiss: () -> Unit, onDiscard: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.discard_changes_title)) },
        text = { Text(stringResource(R.string.discard_changes_message)) },
        confirmButton = {
            TextButton(onClick = onDiscard) {
                Text(stringResource(R.string.discard), color = MaterialTheme.colorScheme.error)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.keep_editing)) }
        },
    )
}

@Composable
internal fun DeleteEventDialog(name: String, enabled: Boolean, onDismiss: () -> Unit, onDelete: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.delete_event_title)) },
        text = { Text(stringResource(R.string.delete_event_message, name)) },
        confirmButton = {
            TextButton(onClick = onDelete, enabled = enabled) {
                Text(stringResource(R.string.delete), color = MaterialTheme.colorScheme.error)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.cancel)) }
        },
    )
}

@Composable
internal fun <T> ChoiceField(
    title: String,
    value: T,
    options: List<T>,
    label: (T) -> Int,
    enabled: Boolean = true,
    onSelected: (T) -> Unit,
) {
    var expanded by rememberSaveable { mutableStateOf(false) }
    ListItem(
        headlineContent = { Text(title) },
        supportingContent = { Text(stringResource(label(value))) },
        trailingContent = { AppIcon(R.drawable.ic_expand) },
        modifier = Modifier.clickable(enabled = enabled, role = Role.Button) { expanded = true },
    )
    if (expanded) {
        AlertDialog(
            onDismissRequest = { expanded = false },
            title = { Text(title) },
            text = {
                Column(Modifier.verticalScroll(rememberScrollState()).selectableGroup()) {
                    options.forEach { option ->
                        Row(
                            Modifier.fillMaxWidth()
                                .selectable(
                                    selected = value == option,
                                    enabled = enabled,
                                    role = Role.RadioButton,
                                    onClick = { onSelected(option); expanded = false },
                                )
                                .padding(vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                            RadioButton(selected = value == option, onClick = null)
                            Text(stringResource(label(option)))
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { expanded = false }) { Text(stringResource(R.string.cancel)) }
            },
        )
    }
}
