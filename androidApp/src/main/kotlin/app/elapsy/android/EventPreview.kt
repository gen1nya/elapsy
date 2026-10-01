package app.elapsy.android

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.text.TextAutoSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.elapsy.core.Event
import app.elapsy.core.Settings
import app.elapsy.core.dayCount
import java.text.NumberFormat
import kotlinx.datetime.LocalDate

internal enum class PreviewShape { SQUARE, RECTANGLE }

@Composable
internal fun EventPreview(
    event: Event,
    settings: Settings,
    today: LocalDate,
    modifier: Modifier = Modifier,
    shape: PreviewShape = PreviewShape.RECTANGLE,
) {
    val resources = LocalResources.current
    val count = event.dayCount(today, settings.firstDay)
    val number = NumberFormat.getIntegerInstance(resources.configuration.locales[0]).format(count.days)
    val caption = EventFormatter.caption(resources, count)
    val date = EventFormatter.dateLine(resources, event, today, settings.dateFormat)
    val name = event.name.ifBlank { stringResource(R.string.event_name_placeholder) }
    val colors = eventColors(
        event.colorArgb,
        event.background,
        MaterialTheme.colorScheme.surfaceContainerHigh,
        MaterialTheme.colorScheme.onSurface,
    )
    val description = stringResource(R.string.event_description, name, number, caption, date)
    val container = modifier
        .clip(RoundedCornerShape(28.dp))
        .background(Brush.linearGradient(colors.background))
        .clearAndSetSemantics { contentDescription = description }
        .padding(20.dp)
    if (shape == PreviewShape.SQUARE) {
        Column(
            modifier = container,
            verticalArrangement = Arrangement.spacedBy(4.dp, Alignment.CenterVertically),
        ) {
            event.icon?.let { Text(it, fontSize = 28.sp) }
            DayNumber(number, colors.number, Modifier.fillMaxWidth().weight(1f), square = true)
            Text(caption, color = colors.content, style = MaterialTheme.typography.bodyMedium)
            Spacer(Modifier.height(4.dp))
            Text(
                name,
                color = colors.content,
                style = MaterialTheme.typography.titleMedium,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
        }
    } else {
        Row(
            modifier = container.heightIn(min = 120.dp).height(IntrinsicSize.Min),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(Modifier.weight(1.1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                event.icon?.let { Text(it, fontSize = 28.sp) }
                Text(
                    name,
                    color = colors.content,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(date, color = colors.content, style = MaterialTheme.typography.bodySmall)
            }
            Spacer(Modifier.width(16.dp))
            VerticalDivider(Modifier.fillMaxHeight(), color = colors.content.copy(alpha = 0.18f))
            Spacer(Modifier.width(16.dp))
            Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                DayNumber(number, colors.number, Modifier.fillMaxWidth())
                Text(
                    caption,
                    color = colors.content,
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                )
            }
        }
    }
}

@Composable
private fun DayNumber(number: String, color: Color, modifier: Modifier, square: Boolean = false) {
    BasicText(
        text = number,
        modifier = modifier,
        maxLines = 1,
        style = MaterialTheme.typography.displayLarge.copy(
            color = color,
            fontWeight = FontWeight.SemiBold,
            textAlign = if (square) TextAlign.Start else TextAlign.Center,
            letterSpacing = (-2).sp,
        ),
        autoSize = TextAutoSize.StepBased(minFontSize = 20.sp, maxFontSize = if (square) 72.sp else 56.sp),
    )
}
