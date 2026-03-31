package xyz.luan.bpb.puzzle

/** Terminal-based interactive UI for the puzzle solver. */
internal class PuzzleTui(private val grid: PuzzleGrid) {

  fun run() {
    render()
    generateSequence {
          print("\nbpb> ")
          readlnOrNull()?.trim()
        }
        .takeWhile { !it.equals("quit", ignoreCase = true) && !it.equals("q", ignoreCase = true) }
        .forEach { input ->
          handleCommand(input)
          render()
        }
  }

  private fun render() {
    print(CLEAR_SCREEN)
    val indent = 2

    println("=== Puzzle Grid ===\n")
    for ((i, row) in grid.rows.withIndex()) {
      val offset = grid.maxLength - row.length
      val pad = " ".repeat(offset + indent)
      val cells = row.cells.mapIndexed { j, ch -> formatCell(ch, row.entry.pattern[j]) }
      val count = row.candidates.size
      println("  ${i + 1}. $pad${cells.joinToString(" ")}  | candidates: $count")
      println("     $pad${row.entry.name}")
      println()
    }

    println("  Final answer: ${grid.getFinalAnswer()}")
    println()
    println("Commands:")
    println("  set <row> <col> <letter>  - fill a cell (e.g. 'set 1 3 A')")
    println("  clear <row> <col>         - clear a cell")
    println("  show <row>                - show candidates for a row")
    println("  quit                      - exit")
  }

  private fun formatCell(ch: Char?, pattern: Char): String {
    val letter = ch?.toString() ?: "_"
    return if (pattern.isLowerCase()) "[$letter]" else " $letter "
  }

  private fun handleCommand(input: String) {
    val parts = input.split("\\s+".toRegex())
    when (parts[0].lowercase()) {
      "set" -> handleSet(parts)
      "clear" -> handleClear(parts)
      "show" -> handleShow(parts)
      else -> pause("Unknown command: '${parts[0]}'. Press Enter to continue.")
    }
  }

  private fun handleSet(parts: List<String>) {
    if (parts.size < SET_ARG_COUNT) {
      pause("Usage: set <row> <col> <letter>")
      return
    }
    val row = (parts[1].toIntOrNull() ?: 0) - 1
    val col = (parts[2].toIntOrNull() ?: 0) - 1
    val letter = parts[LETTER_ARG_INDEX].firstOrNull()?.uppercaseChar()
    val error =
        when {
          row !in grid.rows.indices -> "Invalid row: ${parts[1]}"
          col !in 0 until grid.rows[row].length -> "Invalid col: ${parts[2]}"
          letter == null || !letter.isLetter() -> "Invalid letter: ${parts[LETTER_ARG_INDEX]}"
          else -> null
        }
    if (error != null) {
      pause(error)
      return
    }
    grid.setCell(row, col, letter)
  }

  private fun handleClear(parts: List<String>) {
    if (parts.size < CLEAR_ARG_COUNT) {
      pause("Usage: clear <row> <col>")
      return
    }
    val row = (parts[1].toIntOrNull() ?: 0) - 1
    val col = (parts[2].toIntOrNull() ?: 0) - 1
    if (row !in grid.rows.indices) {
      pause("Invalid row: ${parts[1]}")
      return
    }
    if (col !in 0 until grid.rows[row].length) {
      pause("Invalid col: ${parts[2]}")
      return
    }
    grid.setCell(row, col, null)
  }

  private fun handleShow(parts: List<String>) {
    if (parts.size < SHOW_ARG_COUNT) {
      pause("Usage: show <row>")
      return
    }
    val row = (parts[1].toIntOrNull() ?: 0) - 1
    if (row !in grid.rows.indices) {
      pause("Invalid row: ${parts[1]}")
      return
    }
    val puzzleRow = grid.rows[row]

    val sb = StringBuilder()
    sb.appendLine("Row ${row + 1}: ${puzzleRow.entry.name}")
    sb.appendLine("Pattern: ${puzzleRow.entry.pattern}  Current: ${puzzleRow.getLongWord()}")
    sb.appendLine()
    sb.appendLine("Candidates (${puzzleRow.candidates.size}):")
    val display = puzzleRow.candidates.map { "${it.long} (${it.small})" }
    appendCandidates(sb, display)
    pause(sb.toString())
  }

  private fun appendCandidates(sb: StringBuilder, candidates: List<String>) {
    if (candidates.size <= MAX_DISPLAY) {
      candidates.forEach { sb.appendLine("  $it") }
    } else {
      candidates.take(MAX_DISPLAY).forEach { sb.appendLine("  $it") }
      sb.appendLine("  ... and ${candidates.size - MAX_DISPLAY} more")
    }
  }

  private fun pause(message: String) {
    println(message)
    print("\nPress Enter to continue...")
    readlnOrNull()
  }

  companion object {
    private const val CLEAR_SCREEN = "\u001b[2J\u001b[H"
    private const val MAX_DISPLAY = 50
    private const val SET_ARG_COUNT = 4
    private const val CLEAR_ARG_COUNT = 3
    private const val SHOW_ARG_COUNT = 2
    private const val LETTER_ARG_INDEX = 3
  }
}
