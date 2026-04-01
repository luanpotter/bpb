package xyz.luan.bpb

import com.jakewharton.mosaic.runMosaicBlocking
import xyz.luan.bpb.puzzle.PuzzleEntry
import xyz.luan.bpb.puzzle.PuzzleGrid
import xyz.luan.bpb.ui.PuzzleApp
import xyz.luan.bpb.ui.PuzzleUiState

fun main() {
  val words = DictionaryLoader.load()
  val grid = PuzzleGrid(PuzzleEntry.PUZZLES, words)
  val state = PuzzleUiState(grid)

  runMosaicBlocking { PuzzleApp(state) }
}
