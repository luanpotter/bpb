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

private data class PanelWidths(
    val gridInner: Int,
    val candidateInner: Int,
    val showGrid: Boolean,
    val showCandidate: Boolean,
)

/** Top-level composable for the puzzle application. */
@Composable
internal fun PuzzleApp(state: PuzzleUiState) {
  LaunchedEffect(Unit) { awaitCancellation() }

  val termSize = LocalTerminalState.current.size
  val termCols = termSize.columns
  // Mosaic's AnsiRendering appends \r\n after every row, including the last one.
  // When content height == terminal rows, the trailing \r\n scrolls the viewport,
  // and the first line is lost. Use rows-1 to prevent that scroll.
  val termRows = termSize.rows - 1

  val candidateShown = state.candidateRow != null
  val widths = computePanelWidths(termCols, state.grid.maxLength, candidateShown)
  val panelHeight =
      (termRows - HELP_BAR_HEIGHT - PANEL_BORDER_HEIGHT).coerceAtLeast(state.grid.rows.size * 2 + 2)

  Column(
      modifier =
          Modifier.size(termCols, termRows).onKeyEvent { event -> handleKeyEvent(event, state) },
  ) {
    Row(modifier = Modifier.height(panelHeight + PANEL_BORDER_HEIGHT)) {
      if (widths.showGrid) {
        GridPanel(state, widths.gridInner, panelHeight)
      }
      if (widths.showCandidate) {
        CandidatePanel(state, panelHeight, widths.candidateInner)
      }
    }
    HelpBar(termCols)
    Spacer(
        Modifier.height(
            (termRows - panelHeight - PANEL_BORDER_HEIGHT - HELP_BAR_HEIGHT).coerceAtLeast(0)
        )
    )
  }
}

private fun computePanelWidths(
    termCols: Int,
    maxLength: Int,
    candidateShown: Boolean,
): PanelWidths {
  val minGridInner = 1
  val fullInner = (termCols - BORDER_OVERHEAD).coerceAtLeast(minGridInner)
  if (!candidateShown) return gridOnly(fullInner)

  val naturalGridInner = gridInnerWidth(maxLength)
  val splitAvailableInner = (termCols - BORDER_OVERHEAD * 2).coerceAtLeast(0)

  // If there is enough room for a healthy split, keep grid natural and use a wider sidebar.
  if (splitAvailableInner >= naturalGridInner + MIN_CANDIDATE_INNER_WIDTH) {
    val sidebar =
        (splitAvailableInner - naturalGridInner).coerceAtMost(CANDIDATE_PREFERRED_INNER_WIDTH)
    val grid = (splitAvailableInner - sidebar).coerceAtLeast(minGridInner)
    return splitView(grid, sidebar)
  }

  // Narrow terminal fallback: show candidate panel full-width instead of creating a broken split.
  return sidebarOnly(fullInner)
}

private fun gridOnly(fullInner: Int): PanelWidths =
    PanelWidths(gridInner = fullInner, candidateInner = 0, showGrid = true, showCandidate = false)

private fun splitView(gridInner: Int, candidateInner: Int): PanelWidths =
    PanelWidths(
        gridInner = gridInner,
        candidateInner = candidateInner,
        showGrid = true,
        showCandidate = true,
    )

private fun sidebarOnly(fullInner: Int): PanelWidths =
    PanelWidths(gridInner = 0, candidateInner = fullInner, showGrid = false, showCandidate = true)

private fun handleKeyEvent(event: KeyEvent, state: PuzzleUiState): Boolean {
  if (event.ctrl || event.alt) return false
  return if (state.candidateRow != null) {
    handleCandidateKeys(event, state)
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

private fun handleCandidateKeys(event: KeyEvent, state: PuzzleUiState): Boolean {
  val key = event.key
  if (consumeVimListNavigation(key, state)) return true
  if (consumeArrowListNavigation(key, state)) return true

  return when (state.candidatePanelMode) {
    CandidatePanelMode.CANDIDATES -> handleCandidateListKeys(key, state)
    CandidatePanelMode.SHORT_WORDS -> handleShortWordKeys(key, state)
  }
}

private fun consumeVimListNavigation(key: String, state: PuzzleUiState): Boolean {
  if (key == "G") {
    state.clearSideKeyBuffer()
    state.moveCandidateCursor(Int.MAX_VALUE)
    return true
  }

  if (key == "g") {
    state.pushSideKey("g")
    if (state.sideKeyBuffer == "gg") {
      state.clearSideKeyBuffer()
      state.moveCandidateCursor(Int.MIN_VALUE)
    }
    return true
  }

  state.clearSideKeyBuffer()
  return false
}

private fun consumeArrowListNavigation(key: String, state: PuzzleUiState): Boolean =
    when (key) {
      "ArrowUp" -> {
        state.clearSideKeyBuffer()
        state.moveCandidateCursor(-1)
        true
      }
      "ArrowDown" -> {
        state.clearSideKeyBuffer()
        state.moveCandidateCursor(1)
        true
      }
      else -> false
    }

private fun handleCandidateListKeys(key: String, state: PuzzleUiState): Boolean {
  state.clearSideKeyBuffer()
  when (key) {
    "Enter",
    " " -> state.selectCandidate()
    "Escape",
    "Tab" -> state.dismissCandidates()
    "x",
    "Delete",
    "Backspace" -> state.excludeOrToggleCurrent()
    "s" -> state.switchPanelMode()
    "r" -> state.resetExclusions()
    else -> return false
  }
  return true
}

private fun handleShortWordKeys(key: String, state: PuzzleUiState): Boolean {
  state.clearSideKeyBuffer()
  when (key) {
    "Enter",
    " " -> state.applySelectedShortWord()
    "x" -> state.excludeOrToggleCurrent()
    "Tab",
    "s" -> state.switchPanelMode()
    "Escape" -> state.switchPanelMode()
    "r" -> state.resetExclusions()
    else -> return false
  }
  return true
}
