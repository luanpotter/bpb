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
      return parse(cache.readText())
    }

    val text = URI(DICTIONARY_URL).toURL().readText()
    cache.writeText(text)
    return parse(text)
  }

  fun parse(text: String): List<String> =
      text.lines().filter { it.isNotBlank() }.map { it.trim().uppercase() }
}
