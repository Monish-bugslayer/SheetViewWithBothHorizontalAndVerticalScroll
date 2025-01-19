package com.example.sheetviewwithbothhorizontalandverticalscroll.sheetviewbox

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.lazy.layout.IntervalList
import androidx.compose.foundation.lazy.layout.LazyLayoutItemProvider
import androidx.compose.foundation.lazy.layout.getDefaultLazyLayoutKey
import androidx.compose.runtime.Composable
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import kotlin.math.max

@OptIn(ExperimentalFoundationApi::class)
internal fun rememberItemProvider(content: SheetViewScope.()-> Unit): SheetViewItemProvider =
    run{
        val scope = SheetViewBoxScopeImpl().apply(content)
        SheetViewItemProvider(scope.intervals)
    }


@OptIn(ExperimentalFoundationApi::class)
class SheetViewItemProvider(
    private val intervalList: IntervalList<SheetViewBoxItemContent>,
) : LazyLayoutItemProvider {

    val itemsCacheMap: Map<Int, SheetViewBoxItem> =
        intervalList.pairList { index, localIndex, item ->
            index to item.layoutInfo(localIndex)
        }.toMap()

    private val totalSizeOccupiedByItems: Size get() = run {
        var maxX = 0f
        var maxY = 0f

        itemsCacheMap.forEach { (_, itemInfo) ->
            maxX = max(maxX, itemInfo.x + itemInfo.width.resolve())
            maxY = max(maxY, itemInfo.y+ itemInfo.height.resolve())
        }
        Size(maxX, maxY)
    }

    override val itemCount: Int = intervalList.size

    override fun getKey(index: Int): Any {
        return withLocalIntervalIndex(index){ localIndex, content ->
            content.key?.invoke(localIndex) ?: getDefaultLazyLayoutKey(index)
        }
    }

    override fun getContentType(index: Int): Any? {
        return withLocalIntervalIndex(index){ localIndex, content ->
            content.contentType.invoke(localIndex)
        }
    }

    @Composable
    override fun Item(index: Int, key: Any) {
        withLocalIntervalIndex(index){ localIndex, content ->
            content.content.invoke(localIndex)
        }
    }

    fun getItemsSize(contentPaddingPx: Rect): Size =
    totalSizeOccupiedByItems.let {
        Size(
            width = it.width + contentPaddingPx.left + contentPaddingPx.right,
            height = it.height + contentPaddingPx.top + contentPaddingPx.bottom,
        )
    }


    private fun <T, R> IntervalList<T>.pairList(block: (Int, Int, T) -> R): List<R> = buildList {
        this@pairList.forEach { interval ->
            repeat(interval.size) { index ->
                add(block(index + interval.startIndex, index, interval.value))
            }
        }
    }

    fun getItems(
        translateX: Float,
        translateY: Float,
        contentPadding: Rect,
        size: Size
    ): Map<Int, Rect> {
        val viewport = Rect(
            left = translateX - contentPadding.left,
            top = translateY - contentPadding.top,
            right = translateX - contentPadding.left + size.width,
            bottom = translateY - contentPadding.top + size.height,
        )
        return itemsCacheMap
            .filterValues { it.overlaps(viewport) }
            .mapValues { (_, info) ->
                info.translate(translateX, translateY, contentPadding, viewport)
            }
    }


    private inline fun <T> withLocalIntervalIndex(
        index: Int,
        block: (localIndex: Int, content: SheetViewBoxItemContent) -> T
    ): T {
        val interval = intervalList[index]
        val localIntervalIndex = index - interval.startIndex
        return block(localIntervalIndex, interval.value)
    }
}

