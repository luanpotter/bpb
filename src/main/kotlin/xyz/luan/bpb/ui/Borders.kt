package xyz.luan.bpb.ui

import androidx.compose.runtime.Composable
import com.jakewharton.mosaic.layout.width
import com.jakewharton.mosaic.modifier.Modifier
import com.jakewharton.mosaic.ui.Color
import com.jakewharton.mosaic.ui.Row
import com.jakewharton.mosaic.ui.RowScope
import com.jakewharton.mosaic.ui.Text
import com.jakewharton.mosaic.ui.TextStyle

private val BORDER_COLOR = Color.White
private val BORDER_STYLE = TextStyle.Dim

/** Top border of a panel: ┌─ Title ────────┐ */
@Composable
internal fun BorderTop(title: String, totalWidth: Int) {
  val label = if (title.isNotEmpty()) " $title " else "──"
  val fill = totalWidth - 2 - label.length
  Text("┌─$label${"─".repeat(maxOf(0, fill - 1))}┐", color = BORDER_COLOR, textStyle = BORDER_STYLE)
}

/** Bottom border of a panel: └────────────────┘ */
@Composable
internal fun BorderBottom(totalWidth: Int) {
  Text("└${"─".repeat(totalWidth - 2)}┘", color = BORDER_COLOR, textStyle = BORDER_STYLE)
}

/** A bordered content row with mixed composable content inside fixed inner-width. */
@Composable
internal fun BLine(innerWidth: Int, content: @Composable RowScope.() -> Unit) {
  Row {
    Text("│ ", color = BORDER_COLOR, textStyle = BORDER_STYLE)
    Row(modifier = Modifier.width(innerWidth)) { content() }
    Text(" │", color = BORDER_COLOR, textStyle = BORDER_STYLE)
  }
}

/** A bordered row containing a single styled text, padded to fill. */
@Composable
internal fun BLineText(
    text: String,
    innerWidth: Int,
    color: Color = Color.White,
    textStyle: TextStyle = TextStyle.Unspecified,
) {
  Row {
    Text("│ ", color = BORDER_COLOR, textStyle = BORDER_STYLE)
    Text(text.padEnd(innerWidth), color = color, textStyle = textStyle)
    Text(" │", color = BORDER_COLOR, textStyle = BORDER_STYLE)
  }
}

/** An empty bordered row. */
@Composable
internal fun BLineEmpty(innerWidth: Int) {
  BLineText("", innerWidth)
}
