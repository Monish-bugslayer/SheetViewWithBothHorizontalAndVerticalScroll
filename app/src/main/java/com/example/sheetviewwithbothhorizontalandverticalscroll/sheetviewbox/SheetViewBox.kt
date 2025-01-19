package com.example.sheetviewwithbothhorizontalandverticalscroll.sheetviewbox

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.layout.LazyLayout
import androidx.compose.foundation.lazy.layout.LazyLayoutMeasureScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.input.pointer.PointerInputChange
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.util.VelocityTracker
import androidx.compose.ui.layout.MeasureResult
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlin.math.min

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun SheetViewBox(
    modifier: Modifier,
    state: SheetViewBoxState = rememberSheetViewBoxState(),
    contentPadding: PaddingValues = PaddingValues(0.dp),
    scrollDirection: SheetViewScrollDirection = SheetViewScrollDirection.BOTH,
    content: SheetViewScope.() -> Unit
){
    val coroutineScope = rememberCoroutineScope()
    val contentPaddingPx = contentPadding.toPx()
    val itemProvider = rememberItemProvider(content)
    var positionProvider by remember { mutableStateOf<SheetViewBoxPositionProviderImpl?>(null) }

    LazyLayout(
        itemProvider = {itemProvider},
        modifier = modifier
            .clipToBounds()
            .lazyLayoutPointerInput(state, scrollDirection),
        prefetchState = null,
        measurePolicy = fun LazyLayoutMeasureScope.(constraints: Constraints): MeasureResult{
            val size = Size(constraints.maxWidth.toFloat(), constraints.maxHeight.toFloat())

            positionProvider = positionProvider.update(
                state = state,
                itemProvider = itemProvider,
                layoutDirection = layoutDirection,
                size = size,
                contentPaddingPx = contentPaddingPx,
                scope = coroutineScope
            )

            val items = itemProvider.getItems(
                state.translateX.value,
                state.translateY.value,
                contentPaddingPx,
                size
            )

            val placeables = items.map { (index, bounds) ->
                measure(
                    index,
                    Constraints.fixed(bounds.width.toInt(), bounds.height.toInt())
                ) to bounds.topLeft
            }

            val itemsSize = itemProvider.getItemsSize(contentPaddingPx)
            val width = min(itemsSize.width.toInt(), constraints.maxWidth)
            val height = min(itemsSize.height.toInt(), constraints.maxHeight)

            return layout(width, height) {
                placeables.forEach { (itemPlaceables, position) ->
                    itemPlaceables.forEach { placeable ->
                        placeable.placeRelative(
                            x = position.x.toInt(),
                            y = position.y.toInt(),
                        )
                    }
                }
            }
        }
    )
}

@Composable
private fun PaddingValues.toPx(): Rect {
    val layoutDirection = LocalLayoutDirection.current
    return LocalDensity.current.run {
        Rect(
            calculateLeftPadding(layoutDirection).toPx(),
            calculateTopPadding().toPx(),
            calculateRightPadding(layoutDirection).toPx(),
            calculateBottomPadding().toPx()
        )
    }
}

private fun Modifier.lazyLayoutPointerInput(
    state: SheetViewBoxState,
    scrollDirection: SheetViewScrollDirection,
): Modifier = pointerInput(Unit) {
    val velocityTracker = VelocityTracker()
    coroutineScope {
        when (scrollDirection) {
            SheetViewScrollDirection.BOTH -> detectDragGestures(
                onDragEnd = { onDragEnd(state, velocityTracker, scrollDirection, this) },
                onDrag = { change, dragAmount ->
                    onDrag(state, change, dragAmount, velocityTracker, this)
                }
            )

            SheetViewScrollDirection.HORIZONTAL -> detectHorizontalDragGestures(
                onDragEnd = { onDragEnd(state, velocityTracker, scrollDirection, this) },
                onHorizontalDrag = { change, dragAmount ->
                    onDrag(state, change, Offset(dragAmount, 0f), velocityTracker, this)
                }
            )

            SheetViewScrollDirection.VERTICAL -> detectVerticalDragGestures(
                onDragEnd = { onDragEnd(state, velocityTracker, scrollDirection, this) },
                onVerticalDrag = { change, dragAmount ->
                    onDrag(state, change, Offset(0f, dragAmount), velocityTracker, this)
                }
            )
        }
    }
}

private fun onDrag(
    state: SheetViewBoxState,
    change: PointerInputChange,
    dragAmount: Offset,
    velocityTracker: VelocityTracker,
    scope: CoroutineScope
) {
    change.consume()
    velocityTracker.addPosition(change.uptimeMillis, change.position)
    scope.launch {
        state.dragBy(dragAmount)
    }
}

private fun onDragEnd(
    state: SheetViewBoxState,
    velocityTracker: VelocityTracker,
    scrollDirection: SheetViewScrollDirection,
    scope: CoroutineScope
) {
    var velocity = velocityTracker.calculateVelocity()
    velocity = when (scrollDirection) {
        SheetViewScrollDirection.BOTH -> velocity
        SheetViewScrollDirection.HORIZONTAL -> velocity.copy(velocity.x, 0f)
        SheetViewScrollDirection.VERTICAL -> velocity.copy(0f, velocity.y)
    }
    scope.launch { state.flingBy(velocity) }
}

private fun SheetViewBoxPositionProviderImpl?.update(
    state: SheetViewBoxState,
    itemProvider: SheetViewItemProvider,
    layoutDirection: LayoutDirection,
    size: Size,
    contentPaddingPx: Rect,
    scope: CoroutineScope,
): SheetViewBoxPositionProviderImpl =
    if (
        this != null &&
        this.items == itemProvider.itemsCacheMap && this.layoutDirection == layoutDirection && this.size == size
    ) {
        this
    } else {
        SheetViewBoxPositionProviderImpl(itemProvider.itemsCacheMap, layoutDirection, size).also {
            val itemsSize = itemProvider.getItemsSize(contentPaddingPx)
            val bounds = Rect(
                left = 0f,
                top = 0f,
                right = (itemsSize.width - size.width).coerceAtLeast(0f),
                bottom = (itemsSize.height - size.height).coerceAtLeast(0f)
            )
            state.updateBounds(it, bounds, size, scope)
        }
    }


enum class SheetViewScrollDirection {
    VERTICAL, HORIZONTAL, BOTH
}
