package xyz.luan.bpb

import xyz.luan.bpb.puzzle.PuzzleEntry
import xyz.luan.bpb.puzzle.PuzzleGrid
import xyz.luan.bpb.puzzle.PuzzleTui

fun main() {
  val words = DictionaryLoader.load()
  println("Loaded ${words.size} words.")
  val grid = PuzzleGrid(PuzzleEntry.PUZZLES, words)
  PuzzleTui(grid).run()
}
