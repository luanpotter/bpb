package xyz.luan.bpb.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import xyz.luan.bpb.puzzle.PuzzleGrid

internal enum class CandidatePanelMode {
  CANDIDATES,
  SHORT_WORDS,
}

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

  /** Index of the highlighted candidate in the list. */
  var candidateIdx by mutableIntStateOf(0)
    private set

  /** Which view is active in the candidate panel. */
  var candidatePanelMode by mutableStateOf(CandidatePanelMode.CANDIDATES)
    private set

  /** Index of the highlighted short word in the short words view. */
  var shortWordIdx by mutableIntStateOf(0)
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
      if (cursorCol < row.length - 1) {
        cursorCol++
      } else if (cursorRow < grid.rows.size - 1) {
        cursorRow++
        cursorCol = 0
      }
    }
  }

  fun deleteLetter() {
    val row = grid.rows[cursorRow]
    if (cursorCol in 0 until row.length) {
      grid.setCell(cursorRow, cursorCol, null)
      version++
    }
  }

  fun toggleCandidates() {
    if (candidateRow != null) {
      dismissCandidates()
    } else {
      candidateRow = cursorRow
      candidateIdx = 0
      candidatePanelMode = CandidatePanelMode.CANDIDATES
      shortWordIdx = 0
    }
  }

  fun dismissCandidates() {
    candidateRow = null
    candidatePanelMode = CandidatePanelMode.CANDIDATES
  }

  fun moveCandidateCursor(delta: Int) {
    val rowIdx = candidateRow ?: return
    when (candidatePanelMode) {
      CandidatePanelMode.CANDIDATES -> {
        val count = grid.rows[rowIdx].candidates.size
        if (count == 0) return
        candidateIdx = (candidateIdx + delta).coerceIn(0, count - 1)
      }
      CandidatePanelMode.SHORT_WORDS -> {
        val count = grid.rows[rowIdx].uniqueSmallWords().size
        if (count == 0) return
        shortWordIdx = (shortWordIdx + delta).coerceIn(0, count - 1)
      }
    }
  }

  fun selectCandidate() {
    val rowIdx = candidateRow ?: return
    val row = grid.rows[rowIdx]
    val candidate = row.candidates.getOrNull(candidateIdx) ?: return
    for ((i, ch) in candidate.long.withIndex()) {
      row.setCell(i, ch)
    }
    grid.refreshAll()
    version++
    candidateRow = null
    candidatePanelMode = CandidatePanelMode.CANDIDATES
  }

  fun switchPanelMode() {
    candidatePanelMode =
        when (candidatePanelMode) {
          CandidatePanelMode.CANDIDATES -> {
            shortWordIdx = 0
            CandidatePanelMode.SHORT_WORDS
          }
          CandidatePanelMode.SHORT_WORDS -> {
            candidateIdx =
                candidateIdx.coerceIn(
                    0,
                    (grid.rows[candidateRow ?: return].candidates.size - 1).coerceAtLeast(0),
                )
            CandidatePanelMode.CANDIDATES
          }
        }
  }

  /** Exclude or toggle the currently highlighted item based on active panel mode. */
  fun excludeOrToggleCurrent() {
    val rowIdx = candidateRow ?: return
    val row = grid.rows[rowIdx]
    when (candidatePanelMode) {
      CandidatePanelMode.CANDIDATES -> {
        val candidate = row.candidates.getOrNull(candidateIdx) ?: return
        row.excludedLongWords.add(candidate.long)
        grid.refreshAll()
        version++
        candidateIdx = candidateIdx.coerceIn(0, (row.candidates.size - 1).coerceAtLeast(0))
      }
      CandidatePanelMode.SHORT_WORDS -> {
        val entries = row.uniqueSmallWords()
        val entry = entries.getOrNull(shortWordIdx) ?: return
        if (entry.excluded) {
          row.excludedSmallWords.remove(entry.word)
        } else {
          row.excludedSmallWords.add(entry.word)
        }
        grid.refreshAll()
        version++
      }
    }
  }

  /** Clear all exclusions for the current row. */
  fun resetExclusions() {
    val rowIdx = candidateRow ?: return
    grid.rows[rowIdx].clearExclusions()
    grid.refreshAll()
    version++
    candidateIdx = 0
    shortWordIdx = 0
  }
}
