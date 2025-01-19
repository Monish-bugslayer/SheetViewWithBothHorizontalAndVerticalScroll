package com.example.sheetviewwithbothhorizontalandverticalscroll.sheetviewbox

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.lazy.layout.IntervalList
import androidx.compose.foundation.lazy.layout.MutableIntervalList
import androidx.compose.runtime.Composable

interface SheetViewScope {

    fun sheetViewItem(
        count: Int,
        layoutInfo: (index: Int) -> SheetViewBoxItem,
        key: ((index: Int) -> Any)? = null,
        contentType: (index: Int) -> Any? = { null },
        content: @Composable (index: Int) -> Unit
    )
}

@OptIn(ExperimentalFoundationApi::class)
internal class SheetViewBoxScopeImpl : SheetViewScope {

    private val _intervals = MutableIntervalList<SheetViewBoxItemContent>()
    val intervals: IntervalList<SheetViewBoxItemContent> = _intervals

    override fun sheetViewItem(
        count: Int,
        layoutInfo: (index: Int) -> SheetViewBoxItem,
        key: ((index: Int) -> Any)?,
        contentType: (index: Int) -> Any?,
        content: @Composable (index: Int) -> Unit
    ) {
        _intervals.addInterval(
            count,
            SheetViewBoxItemContent(layoutInfo, key, contentType, content)
        )
    }
}

class SheetViewBoxItemContent(
    val layoutInfo: (index: Int) -> SheetViewBoxItem,
    val key: ((index: Int) -> Any)?,
    val contentType: (index: Int) -> Any?,
    val content: @Composable (index: Int) -> Unit
)