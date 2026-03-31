package xyz.luan.bpb.puzzle

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PuzzleGridTest {
  // A tiny dictionary for testing
  private val words =
      listOf(
          // 3-letter words
          "CAT",
          "BAT",
          "ACE",
          "ARC",
          "ACT",
          // 5-letter words (pattern "XxxxX" -> small at indices 1,2,3)
          "SCATS", // small = CAT
          "SBATS", // small = BAT
          // 6-letter words (pattern "XxxXXX" -> small at indices 1,2)
          "FACTOR", // small = AC, contains C,A,T from CAT
          "CATERS", // small = AT, contains C,A,T from CAT
      )

  @Test
  fun `puzzle row extracts small indices correctly`() {
    val entry = PuzzleEntry("test", "XxxxX")
    val row = PuzzleRow(entry)
    assertEquals(listOf(1, 2, 3), row.smallIndices)
    assertEquals(3, row.smallLength)
    assertEquals(5, row.length)
  }

  @Test
  fun `candidates pair long and small words`() {
    val entry = PuzzleEntry("test", "XxxxX")
    val row = PuzzleRow(entry)
    row.updateCandidates(words, words.toSet())
    // SCATS -> small=CAT (valid), SBATS -> small=BAT (valid)
    assertEquals(2, row.candidates.size)
    assertTrue(row.candidates.any { it.long == "SCATS" && it.small == "CAT" })
    assertTrue(row.candidates.any { it.long == "SBATS" && it.small == "BAT" })
  }

  @Test
  fun `setting a cell filters candidates`() {
    val entry = PuzzleEntry("test", "XxxxX")
    val row = PuzzleRow(entry)
    row.setCell(1, 'C')
    row.updateCandidates(words, words.toSet())
    // Only SCATS matches (small position 0 = C)
    assertEquals(1, row.candidates.size)
    assertEquals("SCATS", row.candidates[0].long)
  }

  @Test
  fun `grid builds with entries`() {
    val entries =
        listOf(
            PuzzleEntry("first", "XxxxX"),
            PuzzleEntry("second", "XxxXXX"),
        )
    val grid = PuzzleGrid(entries, words)
    assertEquals(2, grid.rows.size)
    assertEquals(6, grid.maxLength)
  }

  @Test
  fun `final answer reflects first small letter of each row`() {
    val entries =
        listOf(
            PuzzleEntry("first", "XxxxX"),
            PuzzleEntry("second", "XxxXXX"),
        )
    val grid = PuzzleGrid(entries, words)
    grid.setCell(0, 1, 'A') // small index 0 -> grid col 1
    grid.setCell(1, 1, 'B') // small index 0 -> grid col 1
    assertEquals("AB", grid.getFinalAnswer())
  }
}
