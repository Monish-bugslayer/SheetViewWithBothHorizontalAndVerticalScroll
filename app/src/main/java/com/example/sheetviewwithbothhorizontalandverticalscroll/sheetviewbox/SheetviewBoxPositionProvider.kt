package com.example.sheetviewwithbothhorizontalandverticalscroll.sheetviewbox

import androidx.compose.ui.Alignment
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection


// Returns offset of the item with global [index].
interface SheetViewBoxPositionProvider{
    fun getOffset(
        index: Int,
        alignment: Alignment = Alignment.Center,
        paddingStart: Float = 0f,
        paddingTop: Float = 0f,
        paddingEnd: Float = 0f,
        paddingBottom: Float = 0f,
        currentX: Float = 0f,
        currentY: Float = 0f,
    ): Offset

    fun align(
        itemSize: IntSize,
        alignment: Alignment = Alignment.Center,
        paddingStart: Float = 0f,
        paddingTop: Float = 0f,
        paddingEnd: Float = 0f,
        paddingBottom: Float = 0f,
    ): IntOffset

}

class SheetViewBoxPositionProviderImpl(
    val items: Map<Int, SheetViewBoxItem>,
    val layoutDirection: LayoutDirection,
    val size: Size
): SheetViewBoxPositionProvider {
    override fun getOffset(
        index: Int,
        alignment: Alignment,
        paddingStart: Float,
        paddingTop: Float,
        paddingEnd: Float,
        paddingBottom: Float,
        currentX: Float,
        currentY: Float
    ): Offset {
        val info = items[index] ?: return Offset.Zero
        val itemSize = IntSize(info.width.resolve().toInt(), info.height.resolve().toInt())
        val offset = align(itemSize, alignment, paddingStart, paddingTop, paddingEnd, paddingBottom)
        return Offset(
            if (info.lockHorizontally) currentX else (info.x - offset.x - paddingStart),
            if (info.lockVertically) currentY else (info.y - offset.y - paddingTop)
        )
    }

    override fun align(
        itemSize: IntSize,
        alignment: Alignment,
        paddingStart: Float,
        paddingTop: Float,
        paddingEnd: Float,
        paddingBottom: Float
    ): IntOffset {
        return alignment.align(
            size = IntSize(
                itemSize.width,
                itemSize.height
            ),
            space = IntSize(
                (size.width - paddingStart - paddingEnd).toInt(),
                (size.height - paddingTop - paddingBottom).toInt()
            ),
            layoutDirection = layoutDirection
        )
    }
}