package com.example.unit2026.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

@Composable
fun SwipeableCard(
    onSwipeLeft: () -> Unit,
    onSwipeRight: () -> Unit,
    onOffsetChanged: (Float) -> Unit = {},
    content: @Composable () -> Unit
) {
    val offsetX = remember { Animatable(0f) }
    val offsetY = remember { Animatable(0f) }
    val scope = rememberCoroutineScope()

    Box(
        modifier = Modifier
            .offset { IntOffset(offsetX.value.roundToInt(), offsetY.value.roundToInt()) }
            .graphicsLayer {
                rotationZ = offsetX.value / 20f
            }
            .clip(RoundedCornerShape(24.dp))
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragEnd = {
                        onOffsetChanged(0f)
                        if (offsetX.value > 400) {
                            onSwipeRight()
                            scope.launch {
                                offsetX.snapTo(0f)
                                offsetY.snapTo(0f)
                                onOffsetChanged(0f)
                            }
                        } else if (offsetX.value < -400) {
                            onSwipeLeft()
                            scope.launch {
                                offsetX.snapTo(0f)
                                offsetY.snapTo(0f)
                                onOffsetChanged(0f)
                            }
                        } else {
                            scope.launch {
                                offsetX.animateTo(0f, spring(dampingRatio = Spring.DampingRatioMediumBouncy))
                                onOffsetChanged(0f)
                                offsetY.animateTo(0f, spring(dampingRatio = Spring.DampingRatioMediumBouncy))
                            }
                        }
                    },
                    onDrag = { change, dragAmount ->
                        change.consume()
                        scope.launch {
                            val newX = offsetX.value + dragAmount.x
                            offsetX.snapTo(newX)
                            onOffsetChanged(newX)
                            offsetY.snapTo(offsetY.value + dragAmount.y)
                        }
                    }
                )
            }
    ) {
        content()
    }
}
