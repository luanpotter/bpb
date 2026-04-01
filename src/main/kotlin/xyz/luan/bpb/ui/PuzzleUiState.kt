package xyz.luan.bpb.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import xyz.luan.bpb.puzzle.PuzzleGrid

/** Observable UI state wrapping a [PuzzleGrid] for Compose recomposition. */
internal class PuzzleUiState(val grid: PuzzleGrid) {
  var cursorRow by mutableIntStateOf(0)
    private set

  var cursorCol by mutableIntStateOf(0)
    private set

  /** Bumped on every grid mutation to trigger recomposition. */
  var version by mutableIntStateOf(0)
    private set

  /** When non-null, the candidate panel is shown for this row index. */
  var candidateRow by mutableStateOf<Int?>(null)
    private set

  fun moveCursor(dr: Int, dc: Int) {
    val newRow = (cursorRow + dr).coerceIn(grid.rows.indices)
    cursorRow = newRow
    val maxCol = grid.rows[newRow].length - 1
    cursorCol = (cursorCol + dc).coerceIn(0, maxCol)
  }

  fun typeLetter(ch: Char) {
    val row = grid.rows[cursorRow]
    if (cursorCol in 0 until row.length) {
      grid.setCell(cursorRow, cursorCol, ch)
      version++
      advanceCursor()
    }
  }

  fun deleteLetter() {
    val row = grid.rows[cursorRow]
    if (cursorCol in 0 until row.length) {
      grid.setCell(cursorRow, cursorCol, null)
      version++
      retreatCursor()
    }
  }

  fun toggleCandidates() {
    candidateRow = if (candidateRow == cursorRow) null else cursorRow
  }

  fun dismissCandidates() {
    candidateRow = null
  }

  private fun advanceCursor() {
    val row = grid.rows[cursorRow]
    if (cursorCol < row.length - 1) {
      cursorCol++
    } else if (cursorRow < grid.rows.size - 1) {
      cursorRow++
      cursorCol = 0
    }
  }

  private fun retreatCursor() {
    if (cursorCol > 0) {
      cursorCol--
    } else if (cursorRow > 0) {
      cursorRow--
      cursorCol = grid.rows[cursorRow].length - 1
    }
  }
}
