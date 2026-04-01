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

  /** Optional short-word filter applied while viewing long candidates. */
  var candidateSmallFilter by mutableStateOf<String?>(null)
    private set

  /** Last side-panel keys for vim-like sequences such as gg. */
  var sideKeyBuffer by mutableStateOf("")
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
      candidateSmallFilter = null
      shortWordIdx = 0
      sideKeyBuffer = ""
    }
  }

  fun dismissCandidates() {
    candidateRow = null
    candidatePanelMode = CandidatePanelMode.CANDIDATES
    candidateSmallFilter = null
    sideKeyBuffer = ""
  }

  fun moveCandidateCursor(delta: Int) {
    val rowIdx = candidateRow ?: return
    when (candidatePanelMode) {
      CandidatePanelMode.CANDIDATES -> {
        val filter = candidateSmallFilter
        val candidates =
            if (filter == null) grid.rows[rowIdx].candidates
            else grid.rows[rowIdx].candidates.filter { it.small == filter }
        val count = candidates.size
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
    val filter = candidateSmallFilter
    val visible =
        if (filter == null) row.candidates else row.candidates.filter { it.small == filter }
    val candidate = visible.getOrNull(candidateIdx) ?: return
    for ((i, ch) in candidate.long.withIndex()) {
      row.setCell(i, ch)
    }
    grid.refreshAll()
    version++
    candidateRow = null
    candidatePanelMode = CandidatePanelMode.CANDIDATES
    candidateSmallFilter = null
  }

  fun switchPanelMode() {
    candidatePanelMode =
        when (candidatePanelMode) {
          CandidatePanelMode.CANDIDATES -> {
            candidateSmallFilter = null
            shortWordIdx = 0
            CandidatePanelMode.SHORT_WORDS
          }
          CandidatePanelMode.SHORT_WORDS -> {
            val rowIdx = candidateRow ?: return
            val entries = grid.rows[rowIdx].uniqueSmallWords()
            candidateSmallFilter = entries.getOrNull(shortWordIdx)?.word
            candidateIdx = 0
            CandidatePanelMode.CANDIDATES
          }
        }
    sideKeyBuffer = ""
  }

  /** Fill the selected short-word letters into the row and switch to filtered long candidates. */
  fun applySelectedShortWord() {
    val rowIdx = candidateRow ?: return
    val row = grid.rows[rowIdx]
    val entry = row.uniqueSmallWords().getOrNull(shortWordIdx) ?: return

    for ((i, cellIdx) in row.smallIndices.withIndex()) {
      row.setCell(cellIdx, entry.word[i])
    }

    grid.refreshAll()
    version++
    candidateSmallFilter = entry.word
    candidateIdx = 0
    candidatePanelMode = CandidatePanelMode.CANDIDATES
    sideKeyBuffer = ""
  }

  fun pushSideKey(key: String) {
    sideKeyBuffer = (sideKeyBuffer + key).takeLast(2)
  }

  fun clearSideKeyBuffer() {
    sideKeyBuffer = ""
  }

  /** Exclude or toggle the currently highlighted item based on active panel mode. */
  fun excludeOrToggleCurrent() {
    val rowIdx = candidateRow ?: return
    val row = grid.rows[rowIdx]
    when (candidatePanelMode) {
      CandidatePanelMode.CANDIDATES -> {
        val filter = candidateSmallFilter
        val visible =
            if (filter == null) row.candidates else row.candidates.filter { it.small == filter }
        val candidate = visible.getOrNull(candidateIdx) ?: return
        row.excludedLongWords.add(candidate.long)
        grid.refreshAll()
        version++
        val refreshed =
            if (filter == null) row.candidates else row.candidates.filter { it.small == filter }
        candidateIdx = candidateIdx.coerceIn(0, (refreshed.size - 1).coerceAtLeast(0))
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
    candidateSmallFilter = null
    sideKeyBuffer = ""
  }
}
