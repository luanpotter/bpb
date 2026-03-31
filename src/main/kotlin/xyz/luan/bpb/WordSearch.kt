package xyz.luan.bpb

object WordSearch {
  fun patternToRegex(pattern: String): Regex {
    val escaped = StringBuilder()
    for (ch in pattern.uppercase()) {
      when (ch) {
        '*' -> escaped.append("[A-Z]")
        else -> escaped.append(Regex.escape(ch.toString()))
      }
    }
    return Regex("^$escaped$")
  }

  fun search(words: List<String>, pattern: String): List<String> {
    val regex = patternToRegex(pattern)
    return words.filter { regex.matches(it) }
  }
}
