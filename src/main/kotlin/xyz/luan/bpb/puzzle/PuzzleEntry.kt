package xyz.luan.bpb.puzzle

internal data class PuzzleEntry(
    val name: String,
    val pattern: String,
) {
  companion object {
    val PUZZLES =
        listOf(
            PuzzleEntry("Kept behind locked doors.", "XXxxxX"),
            PuzzleEntry(
                "Can affect one greatly when made by someone with a strong spirit.",
                "XxxxXXX",
            ),
            PuzzleEntry("Makes the validity of a statement clear.", "xxxXXX"),
            PuzzleEntry("On a certain scale, this is very hot", "XXXxxx"),
            PuzzleEntry("Informs you that there's no school today.", "XXXxxx"),
            PuzzleEntry("A group composed of members that have similar characteristics.", "XXxxxx"),
        )
  }
}
