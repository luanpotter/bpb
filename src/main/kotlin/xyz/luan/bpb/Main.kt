package xyz.luan.bpb

import com.jakewharton.mosaic.runMosaicBlocking
import xyz.luan.bpb.puzzle.PuzzleEntry
import xyz.luan.bpb.puzzle.PuzzleGrid
import xyz.luan.bpb.ui.PuzzleApp
import xyz.luan.bpb.ui.PuzzleUiState

fun main() {
  // Load data BEFORE entering alternate screen so any stdout output is visible normally.
  val words = DictionaryLoader.load()
  val grid = PuzzleGrid(PuzzleEntry.PUZZLES, words)
  val state = PuzzleUiState(grid)

  enterAlternateScreen()
  val shutdownHook = Thread { leaveAlternateScreen() }
  Runtime.getRuntime().addShutdownHook(shutdownHook)
  try {
    runMosaicBlocking { PuzzleApp(state) }
  } finally {
    try {
      Runtime.getRuntime().removeShutdownHook(shutdownHook)
    } catch (_: IllegalStateException) {
      // JVM is already shutting down
    }
    leaveAlternateScreen()
  }
}

private fun enterAlternateScreen() {
  // Switch to alternate screen buffer and clear it, hide cursor
  print("\u001b[?1049h\u001b[2J\u001b[H\u001b[?25l")
  System.out.flush()
}

private fun leaveAlternateScreen() {
  // Show cursor, switch back to main screen buffer
  print("\u001b[?25h\u001b[?1049l")
  System.out.flush()
}
