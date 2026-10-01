package app.elapsy.android.widget

import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.glance.ColorFilter
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.ImageProvider
import androidx.glance.LocalContext
import androidx.glance.LocalSize
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.SizeMode
import androidx.glance.appwidget.action.actionStartActivity
import androidx.glance.appwidget.appWidgetBackground
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.color.ColorProvider
import androidx.glance.currentState
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxHeight
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.width
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextAlign
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import app.elapsy.android.ElapsyApp
import app.elapsy.android.EventFormatter
import app.elapsy.android.MainActivity
import app.elapsy.android.R
import app.elapsy.android.eventColors
import app.elapsy.core.BackgroundStyle
import app.elapsy.core.Event
import app.elapsy.core.Settings
import app.elapsy.core.currentDate
import app.elapsy.core.dayCount
import java.text.NumberFormat

class EventWidget : GlanceAppWidget() {
    override val sizeMode = SizeMode.Responsive(setOf(SQUARE, RECTANGLE))

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val repository = (context.applicationContext as ElapsyApp).repository
        val appWidgetId = GlanceAppWidgetManager(context).getAppWidgetId(id)
        provideContent {
            val data by repository.data.collectAsState()
            val eventId = currentState(EVENT_ID)
            // Read only to recompose when the day changes while this session is still alive.
            currentState(DAY)
            val event = data.events.firstOrNull { it.id == eventId }
            if (event == null) {
                SelectEvent(appWidgetId)
            } else {
                EventContent(event, data.settings)
            }
        }
    }

    companion object {
        val EVENT_ID = stringPreferencesKey("event_id")
        val DAY = stringPreferencesKey("day")
        private val SQUARE = DpSize(110.dp, 110.dp)
        private val RECTANGLE = DpSize(250.dp, 110.dp)
        internal val RECTANGLE_MIN_WIDTH = RECTANGLE.width
    }
}

private val LightSurface = Color(0xFFECE6F0)
private val LightOnSurface = Color(0xFF1D1B20)
private val DarkSurface = Color(0xFF2B2930)
private val DarkOnSurface = Color(0xFFE6E0E9)

@Composable
private fun EventContent(event: Event, settings: Settings) {
    val context = LocalContext.current
    val resources = context.resources
    val size = LocalSize.current
    val today = currentDate()
    val count = event.dayCount(today, settings.firstDay)
    val number = NumberFormat.getIntegerInstance(resources.configuration.locales[0]).format(count.days)
    val caption = EventFormatter.caption(resources, count)

    val light = eventColors(event.colorArgb, event.background, LightSurface, LightOnSurface)
    val dark = eventColors(event.colorArgb, event.background, DarkSurface, DarkOnSurface)
    val content = ColorProvider(day = light.content, night = dark.content)
    val numberColor = ColorProvider(day = light.number, night = dark.number)

    val background = if (event.background == BackgroundStyle.GRADIENT) {
        GlanceModifier.background(ImageProvider(gradientBitmap(context, size, light.background)))
    } else {
        GlanceModifier.background(
            ImageProvider(R.drawable.widget_background),
            colorFilter = ColorFilter.tint(ColorProvider(day = light.background.first(), night = dark.background.first())),
        )
    }
    val container = GlanceModifier
        .fillMaxSize()
        .appWidgetBackground()
        .cornerRadius(24.dp)
        .then(background)
        .clickable(actionStartActivity(Intent(context, MainActivity::class.java)))
        .padding(16.dp)

    if (size.width >= EventWidget.RECTANGLE_MIN_WIDTH) {
        Row(container, verticalAlignment = Alignment.CenterVertically) {
            Column(GlanceModifier.defaultWeight()) {
                event.icon?.let { Text(it, style = TextStyle(fontSize = 24.sp)) }
                Text(event.name, style = TextStyle(content, 16.sp, FontWeight.Medium), maxLines = 2)
                Spacer(GlanceModifier.height(4.dp))
                Text(
                    EventFormatter.dateLine(resources, event, today, settings.dateFormat),
                    style = TextStyle(content, 12.sp),
                    maxLines = 1,
                )
            }
            Spacer(GlanceModifier.width(12.dp))
            Box(
                GlanceModifier.width(1.dp).fillMaxHeight().background(
                    ColorProvider(day = light.content.copy(alpha = 0.18f), night = dark.content.copy(alpha = 0.18f))
                )
            ) {}
            Spacer(GlanceModifier.width(12.dp))
            Column(GlanceModifier.defaultWeight(), horizontalAlignment = Alignment.CenterHorizontally) {
                Text(number, style = numberStyle(numberColor, number, large = 44, TextAlign.Center), maxLines = 1)
                Text(caption, style = TextStyle(content, 13.sp, textAlign = TextAlign.Center), maxLines = 1)
            }
        }
    } else {
        Column(container) {
            event.icon?.let { Text(it, style = TextStyle(fontSize = 20.sp)) }
            Spacer(GlanceModifier.defaultWeight())
            Text(number, style = numberStyle(numberColor, number, large = 46, TextAlign.Start), maxLines = 1)
            Text(caption, style = TextStyle(content, 13.sp), maxLines = 1)
            Spacer(GlanceModifier.height(2.dp))
            Text(event.name, style = TextStyle(content, 14.sp, FontWeight.Medium), maxLines = 1)
        }
    }
}

/** Glance cannot auto-size text, so long numbers step down instead. */
private fun numberStyle(color: ColorProvider, number: String, large: Int, align: TextAlign): TextStyle {
    val size = when {
        number.length <= 3 -> large
        number.length == 4 -> large - 8
        else -> large - 18
    }
    return TextStyle(color, size.sp, FontWeight.Bold, textAlign = align)
}

/** Shown before an event is chosen and after the chosen event has been deleted. */
@Composable
private fun SelectEvent(appWidgetId: Int) {
    val context = LocalContext.current
    val configure = Intent(context, WidgetConfigActivity::class.java)
        .putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
        // Distinct data keeps the pending intents of different widgets from collapsing into one.
        .setData(android.net.Uri.parse("elapsy://widget/$appWidgetId"))
    Box(
        modifier = GlanceModifier
            .fillMaxSize()
            .appWidgetBackground()
            .cornerRadius(24.dp)
            .background(
                ImageProvider(R.drawable.widget_background),
                colorFilter = ColorFilter.tint(ColorProvider(day = LightSurface, night = DarkSurface)),
            )
            .clickable(actionStartActivity(configure))
            .padding(16.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            context.getString(R.string.widget_select_event),
            style = TextStyle(
                ColorProvider(day = LightOnSurface, night = DarkOnSurface), 14.sp, FontWeight.Medium,
                textAlign = TextAlign.Center,
            ),
        )
    }
}
