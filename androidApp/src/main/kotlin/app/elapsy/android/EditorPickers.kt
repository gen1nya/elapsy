package app.elapsy.android

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.time.Instant
import java.time.ZoneOffset
import kotlinx.datetime.LocalDate
import kotlinx.datetime.toJavaLocalDate
import kotlinx.datetime.toKotlinLocalDate

internal val eventPalette = listOf(
    0xFFB9C7F4, 0xFFD7C2ED, 0xFFF1BFD2, 0xFFF2BEAE,
    0xFFF2D1A0, 0xFFEAE1A2, 0xFFC8D9A9, 0xFFAFD8C6,
    0xFFA8D7DF, 0xFFACCBE7, 0xFF596B9B, 0xFF596C66,
)

@Composable
internal fun ColorPalette(selected: Long, onSelected: (Long) -> Unit) {
    val names = stringArrayResource(R.array.color_names)
    Column(Modifier.padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(stringResource(R.string.color), style = MaterialTheme.typography.titleSmall)
        FlowRow(
            modifier = Modifier.fillMaxWidth().selectableGroup(),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            eventPalette.forEachIndexed { index, argb ->
                val color = Color(argb.toInt())
                Box(
                    Modifier.size(48.dp).clip(CircleShape)
                        .selectable(selected == argb, role = Role.RadioButton, onClick = { onSelected(argb) })
                        .semantics { contentDescription = names[index] },
                    contentAlignment = Alignment.Center,
                ) {
                    Box(
                        Modifier.size(38.dp).background(color, CircleShape)
                            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, CircleShape),
                        contentAlignment = Alignment.Center,
                    ) {
                        if (selected == argb) {
                            androidx.compose.material3.Icon(
                                painter = androidx.compose.ui.res.painterResource(R.drawable.ic_check),
                                contentDescription = null,
                                tint = contrastingText(color),
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
internal fun EmojiPicker(selected: String?, onSelected: (String?) -> Unit, onDismiss: () -> Unit) {
    val emoji = stringArrayResource(R.array.emoji_presets)
    val names = stringArrayResource(R.array.emoji_names)
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.icon)) },
        text = {
            Column(Modifier.verticalScroll(rememberScrollState()).selectableGroup()) {
                TextButton(onClick = { onSelected(null); onDismiss() }, modifier = Modifier.fillMaxWidth()) {
                    if (selected == null) AppIcon(R.drawable.ic_check)
                    Text(stringResource(R.string.no_icon), modifier = Modifier.padding(8.dp))
                }
                FlowRow(horizontalArrangement = Arrangement.Center, modifier = Modifier.fillMaxWidth()) {
                    emoji.forEachIndexed { index, value ->
                        Box(
                            Modifier.size(48.dp).clip(CircleShape)
                                .background(
                                    if (value == selected) MaterialTheme.colorScheme.secondaryContainer else Color.Transparent
                                )
                                .selectable(
                                    selected = value == selected,
                                    role = Role.RadioButton,
                                    onClick = { onSelected(value); onDismiss() },
                                )
                                .semantics { contentDescription = names[index] },
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(value, fontSize = 26.sp)
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.cancel)) }
        },
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun EventDatePicker(date: LocalDate, onSelected: (LocalDate) -> Unit, onDismiss: () -> Unit) {
    // Material date picker values represent UTC midnight, regardless of the device time zone.
    val state = rememberDatePickerState(
        initialSelectedDateMillis = date.toJavaLocalDate().atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli(),
        yearRange = minOf(1900, date.year)..maxOf(2100, date.year),
    )
    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                enabled = state.selectedDateMillis != null,
                onClick = {
                    state.selectedDateMillis?.let {
                        onSelected(Instant.ofEpochMilli(it).atOffset(ZoneOffset.UTC).toLocalDate().toKotlinLocalDate())
                    }
                    onDismiss()
                },
            ) { Text(stringResource(R.string.done)) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.cancel)) }
        },
    ) { DatePicker(state = state) }
}
