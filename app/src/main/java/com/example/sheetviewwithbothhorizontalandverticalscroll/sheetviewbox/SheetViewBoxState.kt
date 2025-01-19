package com.example.sheetviewwithbothhorizontalandverticalscroll.sheetviewbox

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector1D
import androidx.compose.animation.core.exponentialDecay
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.unit.Velocity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

@Composable
fun rememberSheetViewBoxState(initialOffset: SheetViewBoxPositionProvider.()-> Offset = {Offset.Zero}): SheetViewBoxState{
    return remember { SheetViewBoxState(initialOffset) }
}

class SheetViewBoxState(private val initialOffset: SheetViewBoxPositionProvider.() -> Offset){
    internal lateinit var translateX: Animatable<Float, AnimationVector1D>
    internal lateinit var translateY: Animatable<Float, AnimationVector1D>
    lateinit var positionProvider: SheetViewBoxPositionProvider
    private var translate: Translate? by mutableStateOf(null)
        private set

    private fun updateTranslate(size: Size) {
        if (
            translate == null ||
            translateX.value != translate?.x ||
            translateY.value != translate?.y ||
            translateX.upperBound != translate?.maxX ||
            translateY.upperBound != translate?.maxY
        ) {
            translate = Translate(
                translateX.value,
                translateY.value,
                translateX.upperBound ?: 0f,
                translateY.upperBound ?: 0f,
                size.width,
                size.height
            )
        }
    }

    internal fun updateBounds(
        positionProvider: SheetViewBoxPositionProvider,
        maxBounds: Rect,
        size: Size,
        coroutineScope: CoroutineScope,
    ) {
        this.positionProvider = positionProvider
        if (!::translateX.isInitialized && !::translateY.isInitialized) {
            val (x, y) = positionProvider.initialOffset()
            translateX = Animatable(x)
            translateY = Animatable(y)

            snapshotFlow { translateX.value }
                .onEach { updateTranslate(size) }
                .launchIn(coroutineScope)

            snapshotFlow { translateY.value }
                .onEach { updateTranslate(size) }
                .launchIn(coroutineScope)
        }
        translateX.updateBounds(
            lowerBound = maxBounds.left,
            upperBound = maxBounds.right,
        )
        translateY.updateBounds(
            lowerBound = maxBounds.top,
            upperBound = maxBounds.bottom,
        )
        updateTranslate(size)
    }

    suspend fun flingBy(velocity: Velocity) {
        coroutineScope {
            launch {
                translateX.animateDecay(-velocity.x, exponentialDecay())
            }
            launch {
                translateY.animateDecay(-velocity.y, exponentialDecay())
            }
        }
    }

    suspend fun dragBy(value: Offset) {
        coroutineScope {
            launch {
                // HERE WE CAN ALSO USE snapTo()
                translateX.animateTo(translateX.value - value.x)
            }
            launch {
                translateY.animateTo(translateY.value - value.y)
            }
        }
    }


    //Represent the offset of the plane
    class Translate(
        val x: Float,
        val y: Float,
        val maxX: Float,
        val maxY: Float,
        val viewportWidth: Float,
        val viewportHeight: Float,
    )
}