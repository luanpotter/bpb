package xyz.luan.bpb.puzzle

/**
 * Represents a single row in the puzzle grid. A candidate is a long word (matching the full
 * pattern) whose letters at the lowercase positions form a valid dictionary word.
 */
internal class PuzzleRow(val entry: PuzzleEntry) {
  val length = entry.pattern.length
  val smallIndices = entry.pattern.indices.filter { entry.pattern[it].isLowerCase() }
  val smallLength = smallIndices.size

  /** Current letters in the grid. Null means unfilled. */
  val cells = arrayOfNulls<Char>(length)

  /** Valid (longWord, smallWord) pairs matching current cells and dictionary. */
  var candidates: List<Candidate> = emptyList()
    private set

  fun setCell(index: Int, letter: Char?) {
    cells[index] = letter?.uppercaseChar()
  }

  fun getLongWord(): String = cells.map { it ?: '?' }.joinToString("")

  fun getSmallWord(): String = smallIndices.map { cells[it] ?: '?' }.joinToString("")

  /** Recompute candidates from scratch based on current cells and the dictionary. */
  fun updateCandidates(allWords: List<String>, smallWords: Set<String>) {
    val longMatches = allWords.filter { word ->
      word.length == length && cells.withIndex().all { (i, ch) -> ch == null || word[i] == ch }
    }
    candidates = longMatches.mapNotNull { long ->
      val small = smallIndices.map { long[it] }.joinToString("")
      if (small in smallWords) Candidate(long, small) else null
    }
  }

  /** Prune candidates whose small word letters don't all appear in any next-row long word. */
  fun constrainByNextLong(nextCandidates: List<Candidate>) {
    if (nextCandidates.isEmpty()) return
    val allowedChars = nextCandidates.flatMapTo(mutableSetOf()) { it.long.toList() }
    candidates = candidates.filter { c -> c.small.all { it in allowedChars } }
  }

  /** Prune candidates whose long word doesn't contain required letters from prev-row small. */
  fun constrainByPrevSmall(prevCandidates: List<Candidate>) {
    if (prevCandidates.isEmpty()) return
    val requiredPerPos =
        prevCandidates[0].small.indices.map { pos -> prevCandidates.map { it.small[pos] }.toSet() }
    candidates = candidates.filter { c ->
      val chars = c.long.toSet()
      requiredPerPos.all { possible -> possible.any { it in chars } }
    }
  }

  data class Candidate(val long: String, val small: String)
}
