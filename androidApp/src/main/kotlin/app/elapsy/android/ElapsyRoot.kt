package app.elapsy.android

import android.net.Uri
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.TextButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import app.elapsy.core.AppData
import kotlinx.coroutines.delay

@Composable
internal fun ElapsyRoot(model: ElapsyViewModel, data: AppData) {
    val nav = rememberNavController()
    val snackbar = remember { SnackbarHostState() }
    val resources = LocalResources.current
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    LaunchedEffect(model, lifecycle) {
        lifecycle.repeatOnLifecycle(Lifecycle.State.RESUMED) {
            while (true) {
                model.refreshToday()
                delay(30_000)
            }
        }
    }
    LaunchedEffect(model, resources) {
        model.messages.collect { message ->
            val text = if (message.count == null) resources.getString(message.text)
            else resources.getString(message.text, message.count)
            snackbar.showSnackbar(text)
        }
    }
    val goHome: () -> Unit = { nav.popBackStack("home", inclusive = false) }
    NavHost(navController = nav, startDestination = "home") {
        composable("home") {
            HomeScreen(
                data = data, today = model.today, busy = model.busy, snackbar = snackbar,
                onCreate = { nav.navigate("create") { launchSingleTop = true } },
                onEdit = { nav.navigate("edit/${Uri.encode(it)}") { launchSingleTop = true } },
                onSettings = { nav.navigate("settings") { launchSingleTop = true } },
                onSort = { mode -> model.updateSettings { it.copy(sortMode = mode) } },
                onDuplicate = model::duplicate,
                onDelete = { model.delete(it) },
                onReorder = model::reorder,
            )
        }
        composable("create") {
            EventEditorScreen(
                event = null, settings = data.settings, today = model.today,
                busy = model.busy, snackbar = snackbar, onBack = goHome,
                onSave = { model.save(it, creating = true, onSaved = goHome) },
                onDelete = {},
            )
        }
        composable("edit/{id}", arguments = listOf(navArgument("id") { type = NavType.StringType })) { entry ->
            val event = data.events.firstOrNull { it.id == entry.arguments?.getString("id") }
            if (event == null) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    TextButton(onClick = goHome) { Text(stringResource(R.string.event_unavailable)) }
                }
            } else {
                EventEditorScreen(
                    event = event, settings = data.settings, today = model.today,
                    busy = model.busy, snackbar = snackbar, onBack = goHome,
                    onSave = { model.save(it, creating = false, onSaved = goHome) },
                    onDelete = { model.delete(event.id, onDeleted = goHome) },
                )
            }
        }
        composable("settings") {
            SettingsScreen(data.settings, model, snackbar, goHome)
        }
    }
}
