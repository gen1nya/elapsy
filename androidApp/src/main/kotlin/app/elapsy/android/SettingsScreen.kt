package app.elapsy.android

import android.content.ActivityNotFoundException
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import app.elapsy.core.DateFormatStyle
import app.elapsy.core.FirstDay
import app.elapsy.core.Settings
import app.elapsy.core.ThemeMode

@Composable
internal fun SettingsScreen(
    settings: Settings,
    model: ElapsyViewModel,
    snackbar: SnackbarHostState,
    onBack: () -> Unit,
) {
    val context = LocalContext.current
    val resolver = context.contentResolver
    val exportFileName = stringResource(R.string.export_file_name, model.today.toString())
    val exporter = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/json")) {
        if (it != null) model.exportTo(resolver, it)
    }
    val importer = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) {
        if (it != null) model.importFrom(resolver, it)
    }
    val version = remember(context) {
        @Suppress("DEPRECATION")
        context.packageManager.getPackageInfo(context.packageName, 0).versionName.orEmpty()
    }
    Scaffold(
        topBar = { BackTopBar(stringResource(R.string.settings), onBack) },
        snackbarHost = { SnackbarHost(snackbar) },
    ) { insets ->
        BoxWithConstraints(Modifier.fillMaxSize().padding(insets), contentAlignment = Alignment.TopCenter) {
            Column(
                Modifier.widthIn(max = 640.dp).fillMaxWidth()
                    .verticalScroll(rememberScrollState()).heightIn(min = maxHeight),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                ChoiceField(
                    title = stringResource(R.string.theme), value = settings.theme,
                    options = ThemeMode.entries, label = { it.label() }, enabled = !model.busy,
                    onSelected = { value -> model.updateSettings { it.copy(theme = value) } },
                )
                ChoiceField(
                    title = stringResource(R.string.first_day), value = settings.firstDay,
                    options = FirstDay.entries, label = { it.label() }, enabled = !model.busy,
                    onSelected = { value -> model.updateSettings { it.copy(firstDay = value) } },
                )
                ChoiceField(
                    title = stringResource(R.string.date_format), value = settings.dateFormat,
                    options = DateFormatStyle.entries, label = { it.label() }, enabled = !model.busy,
                    onSelected = { value -> model.updateSettings { it.copy(dateFormat = value) } },
                )
                HorizontalDivider(Modifier.padding(horizontal = 16.dp, vertical = 16.dp))
                Text(
                    stringResource(R.string.data),
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.titleSmall,
                    modifier = Modifier.padding(horizontal = 16.dp),
                )
                ListItem(
                    headlineContent = { Text(stringResource(R.string.export_data)) },
                    supportingContent = { Text(stringResource(R.string.export_description)) },
                    modifier = Modifier.clickable(enabled = !model.busy, role = Role.Button) {
                        try {
                            exporter.launch(exportFileName)
                        } catch (_: ActivityNotFoundException) {
                            model.documentPickerUnavailable()
                        }
                    },
                )
                ListItem(
                    headlineContent = { Text(stringResource(R.string.import_data)) },
                    supportingContent = { Text(stringResource(R.string.import_description)) },
                    modifier = Modifier.clickable(enabled = !model.busy, role = Role.Button) {
                        try {
                            importer.launch(arrayOf("application/json", "text/*", "application/octet-stream"))
                        } catch (_: ActivityNotFoundException) {
                            model.documentPickerUnavailable()
                        }
                    },
                )
                Spacer(Modifier.weight(1f))
                Text(
                    stringResource(R.string.app_version, version),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth().padding(24.dp),
                )
            }
        }
    }
}
