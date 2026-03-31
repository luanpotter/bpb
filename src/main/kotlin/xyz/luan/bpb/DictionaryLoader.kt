package xyz.luan.bpb

import java.io.File
import java.net.URI

object DictionaryLoader {
  private const val DICTIONARY_URL =
      "https://raw.githubusercontent.com/dwyl/english-words/master/words_alpha.txt"
  private const val CACHE_FILE = "dictionary.txt"

  fun load(): List<String> {
    val cache = File(CACHE_FILE)
    if (cache.exists()) {
      println("Loading dictionary from cache...")
      return parse(cache.readText())
    }

    println("Downloading dictionary...")
    val text = URI(DICTIONARY_URL).toURL().readText()
    cache.writeText(text)
    println("Cached to $CACHE_FILE")
    return parse(text)
  }

  fun parse(text: String): List<String> =
      text.lines().filter { it.isNotBlank() }.map { it.trim().uppercase() }
}
