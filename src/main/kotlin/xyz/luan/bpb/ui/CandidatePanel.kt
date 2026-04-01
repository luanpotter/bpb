package xyz.luan.bpb.ui

import androidx.compose.runtime.Composable
import com.jakewharton.mosaic.modifier.Modifier
import com.jakewharton.mosaic.ui.Color
import com.jakewharton.mosaic.ui.Column
import com.jakewharton.mosaic.ui.Row
import com.jakewharton.mosaic.ui.Text
import com.jakewharton.mosaic.ui.TextStyle
import xyz.luan.bpb.puzzle.PuzzleRow

private const val MAX_CANDIDATES = 30

/** Renders the candidate list panel when toggled for a specific row. */
@Composable
internal fun CandidatePanel(state: PuzzleUiState, modifier: Modifier = Modifier) {
  @Suppress("UNUSED_EXPRESSION") state.version
  val rowIdx = state.candidateRow ?: return
  val puzzleRow = state.grid.rows[rowIdx]

  Column(modifier = modifier) {
    Text("")
    CandidateHeader(rowIdx, puzzleRow)
    CandidateList(puzzleRow.candidates)
  }
}

@Composable
private fun CandidateHeader(rowIdx: Int, puzzleRow: PuzzleRow) {
  Row {
    Text("── Candidates for row ${rowIdx + 1}: ", color = Color.Cyan)
    Text(puzzleRow.entry.name, color = Color.Cyan, textStyle = TextStyle.Italic)
    Text(" (${puzzleRow.candidates.size} total) ", color = Color.Cyan)
    Text("──", color = Color.Cyan)
  }
  Text(
      "  Pattern: ${puzzleRow.entry.pattern}  Current: ${puzzleRow.getLongWord()}",
      color = Color.White,
      textStyle = TextStyle.Dim,
  )
  Text("")
}

@Composable
private fun CandidateList(candidates: List<PuzzleRow.Candidate>) {
  val display = candidates.take(MAX_CANDIDATES)
  for (candidate in display) {
    Row {
      Text("  ${candidate.long}", color = Color.White)
      Text(" (${candidate.small})", color = Color.Yellow)
    }
  }
  if (candidates.size > MAX_CANDIDATES) {
    Text(
        "  ... and ${candidates.size - MAX_CANDIDATES} more",
        color = Color.White,
        textStyle = TextStyle.Dim,
    )
  }
}
