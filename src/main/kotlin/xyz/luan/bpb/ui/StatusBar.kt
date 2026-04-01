package xyz.luan.bpb.ui

import androidx.compose.runtime.Composable
import com.jakewharton.mosaic.modifier.Modifier
import com.jakewharton.mosaic.ui.Color
import com.jakewharton.mosaic.ui.Column
import com.jakewharton.mosaic.ui.Row
import com.jakewharton.mosaic.ui.Text
import com.jakewharton.mosaic.ui.TextStyle

/** Renders the final answer and keyboard shortcut help. */
@Composable
internal fun StatusBar(state: PuzzleUiState, modifier: Modifier = Modifier) {
  @Suppress("UNUSED_EXPRESSION") state.version

  Column(modifier = modifier) {
    Text("")
    Row {
      Text("  Answer: ", color = Color.White)
      Text(state.grid.getFinalAnswer(), color = Color.Magenta, textStyle = TextStyle.Bold)
    }
    Text("")
    Row {
      Text("  ← → ↑ ↓", color = Color.Cyan, textStyle = TextStyle.Bold)
      Text(" navigate  ", color = Color.White, textStyle = TextStyle.Dim)
      Text("A-Z", color = Color.Cyan, textStyle = TextStyle.Bold)
      Text(" fill  ", color = Color.White, textStyle = TextStyle.Dim)
      Text("Backspace/Delete", color = Color.Cyan, textStyle = TextStyle.Bold)
      Text(" clear  ", color = Color.White, textStyle = TextStyle.Dim)
      Text("Tab", color = Color.Cyan, textStyle = TextStyle.Bold)
      Text(" candidates  ", color = Color.White, textStyle = TextStyle.Dim)
      Text("Esc", color = Color.Cyan, textStyle = TextStyle.Bold)
      Text(" dismiss  ", color = Color.White, textStyle = TextStyle.Dim)
      Text("Ctrl-C", color = Color.Cyan, textStyle = TextStyle.Bold)
      Text(" quit", color = Color.White, textStyle = TextStyle.Dim)
    }
  }
}
