package xyz.luan.bpb

fun main() {
  val words = DictionaryLoader.load()
  println("Loaded ${words.size} words.")
  println("Enter a pattern (* = any single letter). Type 'quit' to exit.\n")

  generateSequence {
        print("bpb> ")
        readlnOrNull()?.trim()
      }
      .takeWhile { !it.equals("quit", ignoreCase = true) }
      .filter { it.isNotBlank() }
      .forEach { input ->
        val results = WordSearch.search(words, input)
        if (results.isEmpty()) {
          println("No matches.")
        } else {
          println("${results.size} match(es):")
          results.forEach { println("  $it") }
        }
        println()
      }
}
