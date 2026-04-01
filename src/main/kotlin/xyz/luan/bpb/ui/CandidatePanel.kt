package xyz.luan.bpb.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import com.jakewharton.mosaic.ui.Color
import com.jakewharton.mosaic.ui.Column
import com.jakewharton.mosaic.ui.Text
import com.jakewharton.mosaic.ui.TextStyle
import xyz.luan.bpb.puzzle.PuzzleRow

private const val BORDER_OVERHEAD = 4

/** Minimum inner width for the candidate panel. */
internal const val MIN_CANDIDATE_INNER_WIDTH = 32

/** Preferred inner width for the candidate panel on wide terminals. */
internal const val CANDIDATE_PREFERRED_INNER_WIDTH = 44

/** Renders the bordered candidate panel as a side box with selection. */
@Composable
internal fun CandidatePanel(state: PuzzleUiState, panelHeight: Int, innerWidth: Int) {
  val rowIdx = state.candidateRow ?: return
  val puzzleRow = state.grid.rows[rowIdx]
  val totalWidth = innerWidth + BORDER_OVERHEAD

  key(state.version, state.candidatePanelMode, state.candidateIdx, state.shortWordIdx) {
    Column { CandidatePanelContent(state, puzzleRow, rowIdx, panelHeight, totalWidth, innerWidth) }
  }
}

@Composable
private fun CandidatePanelContent(
    state: PuzzleUiState,
    puzzleRow: PuzzleRow,
    rowIdx: Int,
    panelHeight: Int,
    totalWidth: Int,
    innerWidth: Int,
) {
  when (state.candidatePanelMode) {
    CandidatePanelMode.CANDIDATES ->
        CandidateListView(
            puzzleRow,
            rowIdx,
            state.candidateIdx,
            panelHeight,
            totalWidth,
            innerWidth,
            state.candidateSmallFilter,
        )
    CandidatePanelMode.SHORT_WORDS ->
        ShortWordFilterView(
            puzzleRow,
            rowIdx,
            state.shortWordIdx,
            panelHeight,
            totalWidth,
            innerWidth,
        )
  }
}

// ── Candidates list view ───────────────────────────────────────────────────

@Composable
private fun CandidateListView(
    puzzleRow: PuzzleRow,
    rowIdx: Int,
    selectedIdx: Int,
    panelHeight: Int,
    totalWidth: Int,
    innerWidth: Int,
    smallFilter: String?,
) {
  val title =
    if (smallFilter == null) "Candidates · Row ${rowIdx + 1}" else "Candidates · ${smallFilter}"
  val visibleCandidates =
    if (smallFilter == null) puzzleRow.candidates
    else puzzleRow.candidates.filter { it.small == smallFilter }
  BorderTop(title, totalWidth)
  CandidateInfo(puzzleRow, innerWidth, visibleCandidates.size, smallFilter)
  CandidateItems(visibleCandidates, selectedIdx, panelHeight, innerWidth)
  CandidateListHelp(innerWidth)
  BorderBottom(totalWidth)
}

@Composable
private fun CandidateInfo(
    puzzleRow: PuzzleRow,
    innerWidth: Int,
    visibleCount: Int,
    smallFilter: String?,
) {
  BLineText(
    "${puzzleRow.entry.pattern}  ${puzzleRow.getLongWord()}",
    innerWidth,
    color = Color.White,
    textStyle = TextStyle.Dim,
  )
  val exCount = puzzleRow.excludedLongWords.size + puzzleRow.excludedSmallWords.size
  val exSuffix = if (exCount > 0) "  ($exCount excluded)" else ""
  val label =
    if (smallFilter == null) "$visibleCount candidates$exSuffix"
    else "$visibleCount for '$smallFilter'$exSuffix"
  BLineText(label, innerWidth, color = Color.Cyan)
  BLineEmpty(innerWidth)
}

@Composable
private fun CandidateItems(
    candidates: List<PuzzleRow.Candidate>,
    selectedIdx: Int,
    panelHeight: Int,
    innerWidth: Int,
) {
  val listHeight = panelHeight - CANDIDATE_CHROME_LINES
  val visible = computeVisibleWindow(candidates.size, selectedIdx, listHeight)

  for (i in visible) {
    val c = candidates[i]
    val marker = if (i == selectedIdx) "▸ " else "  "
    val text = "$marker${c.long} (${c.small})"
    val color = if (i == selectedIdx) Color.Cyan else Color.White
    val style = if (i == selectedIdx) TextStyle.Bold else TextStyle.Unspecified
    BLineText(text, innerWidth, color = color, textStyle = style)
  }
  repeat(listHeight - visible.count()) { BLineEmpty(innerWidth) }
}

@Composable
private fun CandidateListHelp(innerWidth: Int) {
  BLineEmpty(innerWidth)
  BLine(innerWidth) {
    Text("Enter/Sp", color = Color.Cyan, textStyle = TextStyle.Bold)
    Text(" apply ", color = Color.White, textStyle = TextStyle.Dim)
    Text("x", color = Color.Cyan, textStyle = TextStyle.Bold)
    Text(" exclude ", color = Color.White, textStyle = TextStyle.Dim)
    Text("s", color = Color.Cyan, textStyle = TextStyle.Bold)
    Text(" short words", color = Color.White, textStyle = TextStyle.Dim)
  }
  BLine(innerWidth) {
    Text("gg/G", color = Color.Cyan, textStyle = TextStyle.Bold)
    Text(" jump  ", color = Color.White, textStyle = TextStyle.Dim)
    Text("r", color = Color.Cyan, textStyle = TextStyle.Bold)
    Text(" reset  ", color = Color.White, textStyle = TextStyle.Dim)
    Text("Esc", color = Color.Cyan, textStyle = TextStyle.Bold)
    Text(" close", color = Color.White, textStyle = TextStyle.Dim)
  }
}

// ── Short words filter view ────────────────────────────────────────────────

@Composable
private fun ShortWordFilterView(
    puzzleRow: PuzzleRow,
    rowIdx: Int,
    selectedIdx: Int,
    panelHeight: Int,
    totalWidth: Int,
    innerWidth: Int,
) {
  val title = "Short Words · Row ${rowIdx + 1}"
  BorderTop(title, totalWidth)
  val entries = puzzleRow.uniqueSmallWords()
  BLineText(
    "${entries.count { !it.excluded }} active / ${entries.size} total",
    innerWidth,
    color = Color.Cyan,
  )
  BLineEmpty(innerWidth)
  ShortWordItems(entries, selectedIdx, panelHeight, innerWidth)
  ShortWordHelp(innerWidth)
  BorderBottom(totalWidth)
}

@Composable
private fun ShortWordItems(
    entries: List<PuzzleRow.SmallWordEntry>,
    selectedIdx: Int,
    panelHeight: Int,
    innerWidth: Int,
) {
  val listHeight = panelHeight - SHORT_WORD_CHROME_LINES
  val visible = computeVisibleWindow(entries.size, selectedIdx, listHeight)

  for (i in visible) {
    val e = entries[i]
    val marker = if (i == selectedIdx) "▸ " else "  "
    val status = if (e.excluded) "✗ " else "✓ "
    val text = "$marker$status${e.word} (${e.count})"
    BLineText(
        text,
        innerWidth,
        color = shortWordColor(i == selectedIdx, e.excluded),
        textStyle = shortWordStyle(i == selectedIdx, e.excluded),
    )
  }
  repeat(listHeight - visible.count()) { BLineEmpty(innerWidth) }
}

private fun shortWordColor(selected: Boolean, excluded: Boolean): Color =
    when {
    excluded -> Color.Red
    selected -> Color.Cyan
    else -> Color.White
    }

private fun shortWordStyle(selected: Boolean, excluded: Boolean): TextStyle =
    when {
    excluded -> TextStyle.Dim
    selected -> TextStyle.Bold
    else -> TextStyle.Unspecified
    }

@Composable
private fun ShortWordHelp(innerWidth: Int) {
  BLineEmpty(innerWidth)
  BLine(innerWidth) {
    Text("Enter/Sp", color = Color.Cyan, textStyle = TextStyle.Bold)
    Text(" apply+view ", color = Color.White, textStyle = TextStyle.Dim)
    Text("x", color = Color.Cyan, textStyle = TextStyle.Bold)
    Text(" exclude", color = Color.White, textStyle = TextStyle.Dim)
  }
  BLine(innerWidth) {
    Text("gg/G", color = Color.Cyan, textStyle = TextStyle.Bold)
    Text(" jump  ", color = Color.White, textStyle = TextStyle.Dim)
    Text("Tab", color = Color.Cyan, textStyle = TextStyle.Bold)
    Text(" view longs  ", color = Color.White, textStyle = TextStyle.Dim)
    Text("Esc/s", color = Color.Cyan, textStyle = TextStyle.Bold)
    Text(" back", color = Color.White, textStyle = TextStyle.Dim)
  }
}

// ── Shared helpers ─────────────────────────────────────────────────────────

/** Computes the visible index range for scrolling within a list. */
private fun computeVisibleWindow(total: Int, selected: Int, windowSize: Int): IntRange {
  if (total <= windowSize) return 0 until total
  val half = windowSize / 2
  val start = (selected - half).coerceIn(0, (total - windowSize).coerceAtLeast(0))
  return start until (start + windowSize).coerceAtMost(total)
}

/**
 * Chrome lines in candidate list view: info (3) + help (3) = 6. listHeight = panelHeight - 6 so
 * inner total = 3 + listHeight + 3 = panelHeight.
 */
private const val CANDIDATE_CHROME_LINES = 6

/**
 * Chrome lines in short word view: info (2) + help (3) = 5. listHeight = panelHeight - 5 so inner
 * total = 2 + listHeight + 3 = panelHeight.
 */
private const val SHORT_WORD_CHROME_LINES = 5
