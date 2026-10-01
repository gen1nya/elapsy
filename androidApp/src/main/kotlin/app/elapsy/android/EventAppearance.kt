package app.elapsy.android

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.luminance
import app.elapsy.core.BackgroundStyle

internal data class EventColors(
    val background: List<Color>,
    val content: Color,
    val number: Color,
)

internal fun contrastingText(background: Color): Color =
    if (background.luminance() > 0.179f) Color.Black else Color.White

internal fun eventColors(
    colorArgb: Long,
    style: BackgroundStyle,
    surface: Color,
    onSurface: Color,
): EventColors {
    val color = Color(colorArgb.toInt()).copy(alpha = 1f)
    if (style == BackgroundStyle.SYSTEM) {
        val target = contrastingText(surface)
        var accent = color
        repeat(20) {
            if (contrastRatio(accent, surface) < 4.5f) accent = lerp(accent, target, 0.12f)
        }
        return EventColors(listOf(surface, surface), onSurface, accent)
    }
    val content = contrastingText(color)
    // Move away from the text color so both gradient endpoints retain readable contrast.
    val end = if (style == BackgroundStyle.GRADIENT) {
        lerp(color, if (content == Color.Black) Color.White else Color.Black, 0.28f)
    } else {
        color
    }
    return EventColors(listOf(color, end), content, content)
}

private fun contrastRatio(a: Color, b: Color): Float {
    val first = a.luminance()
    val second = b.luminance()
    return (maxOf(first, second) + 0.05f) / (minOf(first, second) + 0.05f)
}
