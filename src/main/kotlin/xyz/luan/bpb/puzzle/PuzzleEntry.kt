package xyz.luan.bpb.puzzle

internal data class PuzzleEntry(
    val name: String,
    val pattern: String,
) {
  companion object {
    val PUZZLES =
        listOf(
            PuzzleEntry("Kept behind locked doors.", "XXxxxX"),
            PuzzleEntry("Clue 2", "XxxxXXX"),
            PuzzleEntry("Clue 3", "xxxXXX"),
        )
  }
}
