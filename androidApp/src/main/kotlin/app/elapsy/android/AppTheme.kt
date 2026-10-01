package app.elapsy.android

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import app.elapsy.core.ThemeMode

@Composable
internal fun isDarkTheme(mode: ThemeMode): Boolean = when (mode) {
    ThemeMode.SYSTEM -> isSystemInDarkTheme()
    ThemeMode.LIGHT -> false
    ThemeMode.DARK -> true
}

@Composable
internal fun ElapsyTheme(darkTheme: Boolean, content: @Composable () -> Unit) {
    val colors = if (darkTheme) {
        darkColorScheme(
            primary = Color(0xFFB7C4FF),
            onPrimary = Color(0xFF203064),
            primaryContainer = Color(0xFF38477C),
            onPrimaryContainer = Color(0xFFDEE3FF),
            background = Color(0xFF111318),
            surface = Color(0xFF111318),
        )
    } else {
        lightColorScheme(
            primary = Color(0xFF4F6098),
            onPrimary = Color.White,
            primaryContainer = Color(0xFFDEE3FF),
            onPrimaryContainer = Color(0xFF172653),
            background = Color(0xFFF9F9FF),
            surface = Color(0xFFF9F9FF),
        )
    }
    MaterialTheme(colorScheme = colors, content = content)
}
