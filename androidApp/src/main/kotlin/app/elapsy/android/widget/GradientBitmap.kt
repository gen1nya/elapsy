package app.elapsy.android.widget

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Shader
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.DpSize

/** Glance has no gradient brush, so the gradient background is drawn into a rounded bitmap. */
internal fun gradientBitmap(context: Context, size: DpSize, colors: List<Color>): Bitmap {
    val density = context.resources.displayMetrics.density
    val width = (size.width.value * density).toInt().coerceAtLeast(1)
    val height = (size.height.value * density).toInt().coerceAtLeast(1)
    val radius = 24 * density
    val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
    val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        shader = LinearGradient(
            0f, 0f, width.toFloat(), height.toFloat(),
            colors.first().toArgb(), colors.last().toArgb(), Shader.TileMode.CLAMP,
        )
    }
    Canvas(bitmap).drawRoundRect(0f, 0f, width.toFloat(), height.toFloat(), radius, radius, paint)
    return bitmap
}
