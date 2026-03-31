package xyz.luan.bpb.puzzle

/** Manages the full puzzle grid and constraint propagation across rows. */
internal class PuzzleGrid(entries: List<PuzzleEntry>, private val allWords: List<String>) {
  val rows = entries.map { PuzzleRow(it) }

  /** The maximum long-word length across all rows, used for display alignment. */
  val maxLength = rows.maxOf { it.length }

  /** Pre-indexed: words grouped by length, and a set of all words for small-word lookup. */
  private val wordSet = allWords.toSet()

  init {
    refreshAll()
  }

  fun setCell(row: Int, col: Int, letter: Char?) {
    rows[row].setCell(col, letter)
    refreshAll()
  }

  fun refreshAll() {
    for (row in rows) {
      row.updateCandidates(allWords, wordSet)
    }
    repeat(rows.size) {
      var changed = false
      for (i in 0 until rows.size - 1) {
        val before = rows[i].candidates.size
        rows[i].constrainByNextLong(rows[i + 1].candidates)
        if (rows[i].candidates.size != before) changed = true
      }
      for (i in 0 until rows.size - 1) {
        val before = rows[i + 1].candidates.size
        rows[i + 1].constrainByPrevSmall(rows[i].candidates)
        if (rows[i + 1].candidates.size != before) changed = true
      }
      if (!changed) return
    }
  }

  fun getFinalAnswer(): String {
    return rows
        .map { row ->
          val firstSmallIdx = row.smallIndices.firstOrNull() ?: return@map '?'
          row.cells[firstSmallIdx] ?: '?'
        }
        .joinToString("")
  }
}
