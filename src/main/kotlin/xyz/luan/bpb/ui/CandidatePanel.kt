package xyz.luan.bpb.ui

import androidx.compose.runtime.Composable
import com.jakewharton.mosaic.ui.Color
import com.jakewharton.mosaic.ui.Column
import com.jakewharton.mosaic.ui.Text
import com.jakewharton.mosaic.ui.TextStyle
import xyz.luan.bpb.puzzle.PuzzleRow

private const val CANDIDATE_INNER_WIDTH = 32
private const val BORDER_OVERHEAD = 4

/** Total width of the candidate panel including borders. */
internal const val CANDIDATE_PANEL_TOTAL_WIDTH = CANDIDATE_INNER_WIDTH + BORDER_OVERHEAD

/** Renders the bordered candidate panel as a side box with selection. */
@Composable
internal fun CandidatePanel(state: PuzzleUiState, panelHeight: Int) {
  @Suppress("UNUSED_EXPRESSION") state.version
  val rowIdx = state.candidateRow ?: return
  val puzzleRow = state.grid.rows[rowIdx]
  val totalWidth = CANDIDATE_INNER_WIDTH + BORDER_OVERHEAD

  Column {
    val title = "Candidates · Row ${rowIdx + 1}"
    BorderTop(title, totalWidth)
    CandidateInfo(puzzleRow)
    CandidateItems(puzzleRow.candidates, state.candidateIdx, panelHeight)
    CandidateHelp()
    BorderBottom(totalWidth)
  }
}

@Composable
private fun CandidateInfo(puzzleRow: PuzzleRow) {
  BLineText(
      "${puzzleRow.entry.pattern}  ${puzzleRow.getLongWord()}",
      CANDIDATE_INNER_WIDTH,
      color = Color.White,
      textStyle = TextStyle.Dim,
  )
  BLineText(
      "${puzzleRow.candidates.size} candidates",
      CANDIDATE_INNER_WIDTH,
      color = Color.Cyan,
  )
  BLineEmpty(CANDIDATE_INNER_WIDTH)
}

@Composable
private fun CandidateItems(
    candidates: List<PuzzleRow.Candidate>,
    selectedIdx: Int,
    panelHeight: Int,
) {
  val listHeight = panelHeight - CHROME_LINES
  val visible = computeVisibleWindow(candidates.size, selectedIdx, listHeight)

  for (i in visible) {
    val c = candidates[i]
    val marker = if (i == selectedIdx) "▸ " else "  "
    val text = "$marker${c.long} (${c.small})"
    val color = if (i == selectedIdx) Color.Cyan else Color.White
    val style = if (i == selectedIdx) TextStyle.Bold else TextStyle.Unspecified
    BLineText(text, CANDIDATE_INNER_WIDTH, color = color, textStyle = style)
  }
  repeat(listHeight - visible.count()) { BLineEmpty(CANDIDATE_INNER_WIDTH) }
}

@Composable
private fun CandidateHelp() {
  BLineEmpty(CANDIDATE_INNER_WIDTH)
  BLine(CANDIDATE_INNER_WIDTH) {
    Text("↑↓", color = Color.Cyan, textStyle = TextStyle.Bold)
    Text(" select ", color = Color.White, textStyle = TextStyle.Dim)
    Text("Enter", color = Color.Cyan, textStyle = TextStyle.Bold)
    Text(" apply ", color = Color.White, textStyle = TextStyle.Dim)
    Text("Esc", color = Color.Cyan, textStyle = TextStyle.Bold)
    Text(" close", color = Color.White, textStyle = TextStyle.Dim)
  }
}

/** Computes the visible index range for scrolling within the candidate list. */
private fun computeVisibleWindow(total: Int, selected: Int, windowSize: Int): IntRange {
  if (total <= windowSize) return 0 until total
  val half = windowSize / 2
  val start = (selected - half).coerceIn(0, (total - windowSize).coerceAtLeast(0))
  return start until (start + windowSize).coerceAtMost(total)
}

/** Lines used by chrome (info header, help footer, borders are separate). */
private const val CHROME_LINES = 5
