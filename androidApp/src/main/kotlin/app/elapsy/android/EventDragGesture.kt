package app.elapsy.android

import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import sh.calvin.reorderable.DragGestureDetector

@Composable
internal fun rememberEventDragGesture(
    onLongPress: () -> Unit,
    onDrop: () -> Unit,
    onCancel: () -> Unit,
): DragGestureDetector {
    val longPress by rememberUpdatedState(onLongPress)
    val drop by rememberUpdatedState(onDrop)
    val cancel by rememberUpdatedState(onCancel)
    val haptic by rememberUpdatedState(LocalHapticFeedback.current)
    return remember {
        DragGestureDetector { onDragStart, onDragEnd, onDragCancel, onDrag ->
            var distance = Offset.Zero
            var dragging = false
            detectDragGesturesAfterLongPress(
                onDragStart = {
                    distance = Offset.Zero
                    dragging = false
                },
                onDrag = { change, amount ->
                    if (dragging) {
                        onDrag(change, amount)
                    } else {
                        distance += amount
                        if (distance.getDistance() > viewConfiguration.touchSlop) {
                            dragging = true
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            onDragStart(change.position)
                        }
                    }
                },
                onDragEnd = {
                    if (dragging) {
                        onDragEnd()
                        drop()
                    } else {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        longPress()
                    }
                    dragging = false
                },
                onDragCancel = {
                    if (dragging) {
                        onDragCancel()
                        cancel()
                        dragging = false
                    }
                },
            )
        }
    }
}
