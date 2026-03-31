package xyz.luan.bpb

import kotlin.test.Test
import kotlin.test.assertEquals

class WordSearchTest {
  private val words = DictionaryLoader.parse("cat\nhat\nbat\nword\nwild\nwind\nhello\n")

  @Test
  fun `single wildcard matches expected words`() {
    val results = WordSearch.search(words, "*AT")
    assertEquals(listOf("CAT", "HAT", "BAT"), results)
  }

  @Test
  fun `multiple wildcards match expected words`() {
    val results = WordSearch.search(words, "W**D")
    assertEquals(listOf("WORD", "WILD", "WIND"), results)
  }
}
