@file:Suppress("LoopWithTooManyJumpStatements")

package xyz.luan.bpb.ui

import androidx.compose.runtime.Composable
import com.jakewharton.mosaic.ui.Color
import com.jakewharton.mosaic.ui.Column
import com.jakewharton.mosaic.ui.TextStyle

internal const val DEFINITION_DRAWER_BODY_LINES = 4
internal const val DEFINITION_DRAWER_TOTAL_HEIGHT = DEFINITION_DRAWER_BODY_LINES + 2

@Composable
internal fun DefinitionDrawer(totalWidth: Int, text: String, isError: Boolean) {
  val innerWidth = (totalWidth - BORDER_OVERHEAD).coerceAtLeast(1)
  val lines = wrapText(text, innerWidth, DEFINITION_DRAWER_BODY_LINES)

  Column {
    BorderTop("Dictionary", totalWidth)
    for (line in lines) {
      BLineText(
          line,
          innerWidth,
          color = if (isError) Color.Red else Color.White,
          textStyle = if (isError) TextStyle.Dim else TextStyle.Unspecified,
      )
    }
    BorderBottom(totalWidth)
  }
}

private fun wrapText(text: String, width: Int, maxLines: Int): List<String> {
  val out = mutableListOf<String>()
  for (paragraph in text.split("\n")) {
    var remaining = paragraph.trim()
    if (remaining.isEmpty()) {
      out += ""
      if (out.size >= maxLines) break
      continue
    }
    while (remaining.isNotEmpty() && out.size < maxLines) {
      if (remaining.length <= width) {
        out += remaining
        remaining = ""
        break
      }
      var split = remaining.lastIndexOf(' ', width)
      if (split <= 0) split = width
      out += remaining.substring(0, split).trimEnd()
      remaining = remaining.substring(split).trimStart()
    }
    if (out.size >= maxLines) break
  }

  if (out.size < maxLines) {
    repeat(maxLines - out.size) { out += "" }
  }
  return out.take(maxLines)
}

private const val BORDER_OVERHEAD = 4
