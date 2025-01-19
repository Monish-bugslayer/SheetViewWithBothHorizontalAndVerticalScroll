package com.example.sheetviewwithbothhorizontalandverticalscroll

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import com.example.sheetviewwithbothhorizontalandverticalscroll.sheetviewbox.SheetViewBox
import com.example.sheetviewwithbothhorizontalandverticalscroll.sheetviewbox.SheetViewBoxItem
import com.example.sheetviewwithbothhorizontalandverticalscroll.ui.theme.SheetViewWithBothHorizontalAndVerticalScrollTheme

private const val ColumnsCount = 50
private const val RowsCount = 50
private val ItemSize = DpSize(144.dp, 48.dp)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SheetViewWithBothHorizontalAndVerticalScrollTheme {

                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    val itemSizePx = with(LocalDensity.current) { ItemSize.toSize() }
                    SheetViewBox(modifier = Modifier.padding(innerPadding)) {
                        sheetViewItem(count = ColumnsCount * RowsCount, layoutInfo = {
                            val column = it % ColumnsCount
                            val row = it / ColumnsCount
                            SheetViewBoxItem(
                                x = itemSizePx.width * column,
                                y = itemSizePx.height * row,
                                width = itemSizePx.width,
                                height = itemSizePx.height,
                            )
                        }) { index ->
                            Text(
                                text = "Cell #$index",
                                modifier = Modifier
                                    .border(1.dp, MaterialTheme.colorScheme.primary)
                                    .padding(8.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
