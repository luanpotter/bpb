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
private const val HINT_SPACER_WIDTH = 3
private const val HINT_RESERVED_WIDTH = 96

/** Computes the inner content width for the grid panel. */
internal fun gridInnerWidth(maxLength: Int): Int =
    ROW_NUM_WIDTH +
        maxLength * CELL_WIDTH +
        COUNT_SUFFIX_WIDTH +
        HINT_SPACER_WIDTH +
        HINT_RESERVED_WIDTH

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
      }
      val usedLines = grid.rows.size + 2
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
  val isSelectedRow = rowIdx == state.cursorRow
  BLine(innerWidth) {
    val offset = maxLength - row.length
    val pad = " ".repeat(offset * CELL_WIDTH)
    val rowLabel = "${(rowIdx + 1).toString().padStart(2)}. "
    Text(
        rowLabel,
        color = if (isSelectedRow) Color.Cyan else Color.White,
        textStyle = if (isSelectedRow) TextStyle.Bold else TextStyle.Dim,
    )
    Text(pad, color = Color.White, textStyle = TextStyle.Dim)

    for (colIdx in 0 until row.length) {
      CellView(
          row.cells[colIdx],
          row.entry.pattern[colIdx].isLowerCase(),
          rowIdx == state.cursorRow && colIdx == state.cursorCol,
      )
    }

    val countStr = row.candidates.size.toString().padStart(COUNT_SUFFIX_WIDTH - CELL_WIDTH)
    Text(" │$countStr", color = Color.White, textStyle = TextStyle.Dim)

    val clue = row.entry.name.padEnd(HINT_RESERVED_WIDTH)
    Text(" │ ", color = Color.White, textStyle = TextStyle.Dim)
    Text(
        clue,
        color = if (isSelectedRow) Color.Cyan else Color.White,
        textStyle = if (isSelectedRow) TextStyle.Bold else TextStyle.Dim,
    )
  }
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
