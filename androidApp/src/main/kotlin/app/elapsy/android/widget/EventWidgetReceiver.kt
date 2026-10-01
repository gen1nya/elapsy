package app.elapsy.android.widget

import android.app.AlarmManager
import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.state.updateAppWidgetState
import app.elapsy.core.currentDate
import java.time.LocalDate
import java.time.ZoneId
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class EventWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = EventWidget()

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        when (intent.action) {
            ACTION_DAY_CHANGED, Intent.ACTION_TIME_CHANGED, Intent.ACTION_TIMEZONE_CHANGED -> {
                scheduleNextDayRefresh(context)
                val pending = goAsync()
                CoroutineScope(Dispatchers.Default).launch {
                    try {
                        refreshAll(context)
                    } finally {
                        pending.finish()
                    }
                }
            }
        }
    }

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        super.onUpdate(context, appWidgetManager, appWidgetIds)
        // Also runs after a reboot, which drops pending alarms.
        scheduleNextDayRefresh(context)
    }

    override fun onDisabled(context: Context) {
        super.onDisabled(context)
        context.getSystemService(AlarmManager::class.java).cancel(dayChangedIntent(context))
    }

    companion object {
        private const val ACTION_DAY_CHANGED = "app.elapsy.action.DAY_CHANGED"

        /** Re-renders every widget for the current date. */
        suspend fun refreshAll(context: Context) {
            val today = currentDate().toString()
            GlanceAppWidgetManager(context).getGlanceIds(EventWidget::class.java).forEach { id ->
                updateAppWidgetState(context, id) { it[EventWidget.DAY] = today }
                EventWidget().update(context, id)
            }
        }

        /**
         * Day counts change at local midnight. A non-wakeup alarm is enough: it fires as soon as the
         * device is next awake, which is before anyone can look at the widget.
         */
        fun scheduleNextDayRefresh(context: Context) {
            val midnight = LocalDate.now().plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
            context.getSystemService(AlarmManager::class.java)
                .setAndAllowWhileIdle(AlarmManager.RTC, midnight, dayChangedIntent(context))
        }

        private fun dayChangedIntent(context: Context): PendingIntent = PendingIntent.getBroadcast(
            context,
            0,
            Intent(context, EventWidgetReceiver::class.java).setAction(ACTION_DAY_CHANGED),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
    }
}
