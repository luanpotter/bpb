package xyz.luan.bpb.ui

import androidx.compose.runtime.Composable
import com.jakewharton.mosaic.modifier.Modifier
import com.jakewharton.mosaic.ui.Color
import com.jakewharton.mosaic.ui.Column
import com.jakewharton.mosaic.ui.Row
import com.jakewharton.mosaic.ui.Text
import com.jakewharton.mosaic.ui.TextStyle
import xyz.luan.bpb.puzzle.PuzzleRow

/** Renders the puzzle grid with cursor highlighting and candidate counts. */
@Composable
internal fun GridView(state: PuzzleUiState, modifier: Modifier = Modifier) {
  @Suppress("UNUSED_EXPRESSION") state.version
  val grid = state.grid

  Column(modifier = modifier) {
    for ((rowIdx, puzzleRow) in grid.rows.withIndex()) {
      val offset = grid.maxLength - puzzleRow.length
      GridRow(puzzleRow, rowIdx, offset, state)
    }
  }
}

@Composable
private fun GridRow(puzzleRow: PuzzleRow, rowIdx: Int, offset: Int, state: PuzzleUiState) {
  val pad = " ".repeat(offset)
  Row {
    Text("  ${rowIdx + 1}. $pad", color = Color.White, textStyle = TextStyle.Dim)
    for (colIdx in 0 until puzzleRow.length) {
      val ch = puzzleRow.cells[colIdx]
      val isSmall = puzzleRow.entry.pattern[colIdx].isLowerCase()
      val isCursor = rowIdx == state.cursorRow && colIdx == state.cursorCol
      CellView(ch, isSmall, isCursor)
    }
    Text("  │ ${puzzleRow.candidates.size}", color = Color.White, textStyle = TextStyle.Dim)
  }
  Row {
    val clueOffset = " ".repeat(offset + CLUE_INDENT)
    Text("     $clueOffset${puzzleRow.entry.name}", color = Color.White, textStyle = TextStyle.Dim)
  }
}

/** Renders a single grid cell with cursor and small-word highlighting. */
@Composable
private fun CellView(ch: Char?, isSmall: Boolean, isCursor: Boolean) {
  val letter = ch?.toString() ?: "_"
  val cellText = if (isSmall) "[$letter]" else " $letter "

  when {
    isCursor ->
        Text(cellText, color = Color.Black, background = Color.Cyan, textStyle = TextStyle.Bold)
    ch != null && isSmall -> Text(cellText, color = Color.Yellow, textStyle = TextStyle.Bold)
    ch != null -> Text(cellText, color = Color.Green)
    isSmall -> Text(cellText, color = Color.White, textStyle = TextStyle.Dim)
    else -> Text(cellText, color = Color.White, textStyle = TextStyle.Dim)
  }
}

private const val CLUE_INDENT = 2
