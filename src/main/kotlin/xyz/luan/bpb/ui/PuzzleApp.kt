package xyz.luan.bpb.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import com.jakewharton.mosaic.LocalTerminalState
import com.jakewharton.mosaic.layout.KeyEvent
import com.jakewharton.mosaic.layout.height
import com.jakewharton.mosaic.layout.onKeyEvent
import com.jakewharton.mosaic.layout.size
import com.jakewharton.mosaic.modifier.Modifier
import com.jakewharton.mosaic.ui.Column
import com.jakewharton.mosaic.ui.Row
import com.jakewharton.mosaic.ui.Spacer
import kotlinx.coroutines.awaitCancellation

private const val BORDER_OVERHEAD = 4
private const val HELP_BAR_HEIGHT = 3
private const val PANEL_BORDER_HEIGHT = 2

/** Top-level composable for the puzzle application. */
@Composable
internal fun PuzzleApp(state: PuzzleUiState) {
  LaunchedEffect(Unit) { awaitCancellation() }

  val termSize = LocalTerminalState.current.size
  val termCols = termSize.columns
  // Mosaic's AnsiRendering appends \r\n after every row including the last one.
  // When content height == terminal rows, the trailing \r\n scrolls the viewport
  // and the first line is lost. Use rows-1 to prevent that scroll.
  val termRows = termSize.rows - 1

  val candidateShown = state.candidateRow != null
  val candidateTotalWidth = CANDIDATE_PANEL_TOTAL_WIDTH
  val gridTotalWidth = if (candidateShown) termCols - candidateTotalWidth else termCols
  val gridInner =
      (gridTotalWidth - BORDER_OVERHEAD).coerceAtLeast(gridInnerWidth(state.grid.maxLength))
  val panelHeight =
      (termRows - HELP_BAR_HEIGHT - PANEL_BORDER_HEIGHT).coerceAtLeast(state.grid.rows.size * 2 + 2)

  Column(
      modifier =
          Modifier.size(termCols, termRows).onKeyEvent { event -> handleKeyEvent(event, state) },
  ) {
    Row(modifier = Modifier.height(panelHeight + PANEL_BORDER_HEIGHT)) {
      GridPanel(state, gridInner, panelHeight)
      CandidatePanel(state, panelHeight)
    }
    HelpBar(termCols)
    Spacer(
        Modifier.height(
            (termRows - panelHeight - PANEL_BORDER_HEIGHT - HELP_BAR_HEIGHT).coerceAtLeast(0)
        )
    )
  }
}

private fun handleKeyEvent(event: KeyEvent, state: PuzzleUiState): Boolean {
  if (event.ctrl || event.alt) return false
  return if (state.candidateRow != null) {
    handleCandidateKeys(event.key, state)
  } else {
    handleGridKeys(event.key, state)
  }
}

private fun handleGridKeys(key: String, state: PuzzleUiState): Boolean {
  when (key) {
    "ArrowUp" -> state.moveCursor(-1, 0)
    "ArrowDown" -> state.moveCursor(1, 0)
    "ArrowLeft" -> state.moveCursor(0, -1)
    "ArrowRight" -> state.moveCursor(0, 1)
    "Tab" -> state.toggleCandidates()
    "Enter" -> state.moveCursor(1, 0)
    "Backspace",
    "Delete" -> state.deleteLetter()
    else -> return handleLetterInput(key, state)
  }
  return true
}

private fun handleLetterInput(key: String, state: PuzzleUiState): Boolean {
  if (key.length == 1 && key[0].isLetter()) {
    state.typeLetter(key[0].uppercaseChar())
    return true
  }
  return false
}

private fun handleCandidateKeys(key: String, state: PuzzleUiState): Boolean {
  when (key) {
    "ArrowUp" -> state.moveCandidateCursor(-1)
    "ArrowDown" -> state.moveCandidateCursor(1)
    "Enter" -> state.selectCandidate()
    "Escape",
    "Tab" -> state.dismissCandidates()
    else -> return false
  }
  return true
}
