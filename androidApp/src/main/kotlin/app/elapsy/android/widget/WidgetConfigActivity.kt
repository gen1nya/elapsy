package app.elapsy.android.widget

import android.appwidget.AppWidgetManager
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.state.updateAppWidgetState
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import app.elapsy.android.ElapsyApp
import app.elapsy.android.ElapsyTheme
import app.elapsy.android.EventPreview
import app.elapsy.android.MainActivity
import app.elapsy.android.R
import app.elapsy.android.isDarkTheme
import app.elapsy.core.currentDate
import app.elapsy.core.sorted
import kotlinx.coroutines.launch

/** Lets the user pick which event a widget shows, both when it is added and when it is reconfigured. */
class WidgetConfigActivity : ComponentActivity() {
    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val appWidgetId = intent.getIntExtra(
            AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID
        )
        // Backing out of this screen must not leave a half-added widget behind.
        setResult(RESULT_CANCELED, Intent().putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId))
        if (appWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            finish()
            return
        }
        val repository = (application as ElapsyApp).repository
        setContent {
            val data by repository.data.collectAsStateWithLifecycle()
            val today = currentDate()
            val dark = isDarkTheme(data.settings.theme)
            SideEffect {
                val style = SystemBarStyle.auto(Color.TRANSPARENT, Color.TRANSPARENT) { dark }
                enableEdgeToEdge(statusBarStyle = style, navigationBarStyle = style)
            }
            ElapsyTheme(dark) {
                Scaffold(topBar = { TopAppBar(title = { Text(stringResource(R.string.widget_config_title)) }) }) { insets ->
                    if (data.events.isEmpty()) {
                        Column(
                            Modifier.fillMaxSize().padding(insets).padding(32.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically),
                            horizontalAlignment = Alignment.CenterHorizontally,
                        ) {
                            Text(
                                stringResource(R.string.widget_config_empty),
                                style = MaterialTheme.typography.bodyLarge,
                                textAlign = TextAlign.Center,
                            )
                            Button(onClick = {
                                startActivity(Intent(this@WidgetConfigActivity, MainActivity::class.java))
                                finish()
                            }) { Text(stringResource(R.string.widget_config_open_app)) }
                        }
                    } else {
                        val events = data.events.sorted(data.settings.sortMode, today, data.settings.firstDay)
                        LazyColumn(
                            Modifier.fillMaxSize().padding(insets),
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                            items(events, key = { it.id }) { event ->
                                EventPreview(
                                    event, data.settings, today,
                                    Modifier.fillMaxWidth().clip(RoundedCornerShape(28.dp)).clickable { select(appWidgetId, event.id) },
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    private fun select(appWidgetId: Int, eventId: String) {
        lifecycleScope.launch {
            val context = this@WidgetConfigActivity
            val glanceId = GlanceAppWidgetManager(context).getGlanceIdBy(appWidgetId)
            updateAppWidgetState(context, glanceId) { it[EventWidget.EVENT_ID] = eventId }
            EventWidget().update(context, glanceId)
            EventWidgetReceiver.scheduleNextDayRefresh(context)
            setResult(RESULT_OK, Intent().putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId))
            finish()
        }
    }
}
