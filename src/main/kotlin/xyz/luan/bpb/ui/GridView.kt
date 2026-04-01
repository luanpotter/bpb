package xyz.luan.bpb.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import com.jakewharton.mosaic.ui.Color
import com.jakewharton.mosaic.ui.Column
import com.jakewharton.mosaic.ui.Text
import com.jakewharton.mosaic.ui.TextStyle
import xyz.luan.bpb.puzzle.PuzzleRow

private const val CELL_WIDTH = 3
private const val ROW_NUM_WIDTH = 5
private const val COUNT_SUFFIX_WIDTH = 10

/** Computes the inner content width for the grid panel. */
internal fun gridInnerWidth(maxLength: Int): Int =
    ROW_NUM_WIDTH + maxLength * CELL_WIDTH + COUNT_SUFFIX_WIDTH

/** Renders the bordered grid panel. */
@Composable
internal fun GridPanel(state: PuzzleUiState, innerWidth: Int, panelHeight: Int) {
  val grid = state.grid
  val totalWidth = innerWidth + BORDER_OVERHEAD

  key(state.version) {
    Column {
      BorderTop("Puzzle Grid", totalWidth)
      for ((rowIdx, puzzleRow) in grid.rows.withIndex()) {
        GridCellRow(puzzleRow, rowIdx, grid.maxLength, innerWidth, state)
        GridClueRow(puzzleRow, grid.maxLength, innerWidth)
      }
      val usedLines = grid.rows.size * 2 + 2
      repeat(panelHeight - usedLines) { BLineEmpty(innerWidth) }
      BLineEmpty(innerWidth)
      GridAnswerRow(state, innerWidth)
      BorderBottom(totalWidth)
    }
  }
}

@Composable
private fun GridCellRow(
    row: PuzzleRow,
    rowIdx: Int,
    maxLength: Int,
    innerWidth: Int,
    state: PuzzleUiState,
) {
  BLine(innerWidth) {
    val offset = maxLength - row.length
    val pad = " ".repeat(offset * CELL_WIDTH)
    Text(
        "${(rowIdx + 1).toString().padStart(2)}. $pad",
        color = Color.White,
        textStyle = TextStyle.Dim,
    )
    for (colIdx in 0 until row.length) {
      CellView(
          row.cells[colIdx],
          row.entry.pattern[colIdx].isLowerCase(),
          rowIdx == state.cursorRow && colIdx == state.cursorCol,
      )
    }
    val countStr = row.candidates.size.toString().padStart(COUNT_SUFFIX_WIDTH - CELL_WIDTH)
    Text(" │$countStr", color = Color.White, textStyle = TextStyle.Dim)
  }
}

@Composable
private fun GridClueRow(row: PuzzleRow, maxLength: Int, innerWidth: Int) {
  val offset = maxLength - row.length
  val prefix = " ".repeat(ROW_NUM_WIDTH + offset * CELL_WIDTH)
  BLineText("$prefix${row.entry.name}", innerWidth, color = Color.White, textStyle = TextStyle.Dim)
}

@Composable
private fun GridAnswerRow(state: PuzzleUiState, innerWidth: Int) {
  BLine(innerWidth) {
    Text("  Answer: ", color = Color.White)
    Text(state.grid.getFinalAnswer(), color = Color.Magenta, textStyle = TextStyle.Bold)
  }
}

@Composable
private fun CellView(ch: Char?, isSmall: Boolean, isCursor: Boolean) {
  val letter = ch?.toString() ?: "_"
  val cellText = if (isSmall) "[$letter]" else " $letter "
  when {
    isCursor ->
        Text(cellText, color = Color.Black, background = Color.Cyan, textStyle = TextStyle.Bold)
    ch != null && isSmall -> Text(cellText, color = Color.Yellow, textStyle = TextStyle.Bold)
    ch != null -> Text(cellText, color = Color.Green)
    else -> Text(cellText, color = Color.White, textStyle = TextStyle.Dim)
  }
}

private const val BORDER_OVERHEAD = 4
