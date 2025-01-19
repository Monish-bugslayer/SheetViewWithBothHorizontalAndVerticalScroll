package com.example.sheetviewwithbothhorizontalandverticalscroll.sheetviewbox

import androidx.compose.ui.geometry.Rect

class SheetViewBoxItem(
    val x: Float,
    val y: Float,
    val width: Value,
    val height: Value,
    val lockHorizontally: Boolean = false,
    val lockVertically: Boolean = false,
) {
    constructor(
        x: Float,
        y: Float,
        width: Float,
        height: Float,
        lockHorizontally: Boolean = false,
        lockVertically: Boolean = false,
    ) : this(x, y, Value.Absolute(width), Value.Absolute(height), lockHorizontally, lockVertically)


    sealed interface Value {
        class Absolute(val value: Float) : Value
        class MatchParent(val fraction: Float) : Value

        fun resolve(parentSize: Float = 0f): Float = when (this) {
            is Absolute -> value
            is MatchParent -> parentSize * fraction
        }
    }

    fun overlaps(other: Rect): Boolean {
        if (lockVertically && lockHorizontally) {
            return true
        }

        val width = width.resolve(other.width)
        val height = height.resolve(other.height)
        fun overlapsHorizontally(): Boolean = x + width > other.left && x < other.right
        fun overlapsVertically(): Boolean = y + height > other.top && y < other.bottom

        return if (lockHorizontally) {
            overlapsVertically()
        } else if (lockVertically) {
            overlapsHorizontally()
        } else {
            overlapsHorizontally() && overlapsVertically()
        }
    }

    fun translate(
        translateX: Float,
        translateY: Float,
        contentPadding: Rect,
        viewport: Rect
    ): Rect {
        val itemTranslateX = translateX.takeUnless { lockHorizontally } ?: 0f
        val itemTranslateY = translateY.takeUnless { lockVertically } ?: 0f
        val newX = this.x - itemTranslateX + contentPadding.left
        val newY = this.y - itemTranslateY + contentPadding.top
        val width = width.resolve(viewport.width - contentPadding.left - contentPadding.right)
        val height = height.resolve(viewport.height - contentPadding.top - contentPadding.bottom)
        return Rect(
            left = newX,
            top = newY,
            right = newX + width,
            bottom = newY + height
        )
    }
}
