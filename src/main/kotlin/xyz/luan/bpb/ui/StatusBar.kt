package xyz.luan.bpb.ui

import androidx.compose.runtime.Composable
import com.jakewharton.mosaic.ui.Color
import com.jakewharton.mosaic.ui.Column
import com.jakewharton.mosaic.ui.Text
import com.jakewharton.mosaic.ui.TextStyle

/** Renders the bordered keyboard shortcut help bar. */
@Composable
internal fun HelpBar(totalWidth: Int) {
  val innerWidth = totalWidth - BORDER_OVERHEAD
  Column {
    BorderTop("Keys", totalWidth)
    BLine(innerWidth) {
      Text("←→↑↓", color = Color.Cyan, textStyle = TextStyle.Bold)
      Text(" navigate  ", color = Color.White, textStyle = TextStyle.Dim)
      Text("A-Z", color = Color.Cyan, textStyle = TextStyle.Bold)
      Text(" fill  ", color = Color.White, textStyle = TextStyle.Dim)
      Text("BS", color = Color.Cyan, textStyle = TextStyle.Bold)
      Text(" clear  ", color = Color.White, textStyle = TextStyle.Dim)
      Text("Tab", color = Color.Cyan, textStyle = TextStyle.Bold)
      Text(" candidates  ", color = Color.White, textStyle = TextStyle.Dim)
      Text("Ctrl-C", color = Color.Cyan, textStyle = TextStyle.Bold)
      Text(" quit", color = Color.White, textStyle = TextStyle.Dim)
    }
    BorderBottom(totalWidth)
  }
}

private const val BORDER_OVERHEAD = 4
