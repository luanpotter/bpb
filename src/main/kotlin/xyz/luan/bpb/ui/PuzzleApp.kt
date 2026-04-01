package xyz.luan.bpb.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import com.jakewharton.mosaic.layout.KeyEvent
import com.jakewharton.mosaic.layout.onKeyEvent
import com.jakewharton.mosaic.modifier.Modifier
import com.jakewharton.mosaic.ui.Color
import com.jakewharton.mosaic.ui.Column
import com.jakewharton.mosaic.ui.Text
import com.jakewharton.mosaic.ui.TextStyle
import kotlinx.coroutines.awaitCancellation

/** Top-level composable for the puzzle application. */
@Composable
internal fun PuzzleApp(state: PuzzleUiState) {
  LaunchedEffect(Unit) { awaitCancellation() }

  Column(
      modifier = Modifier.onKeyEvent { event -> handleKeyEvent(event, state) },
  ) {
    Text("")
    Text("  ═══ Puzzle Grid ═══", color = Color.Cyan, textStyle = TextStyle.Bold)
    Text("")
    GridView(state)
    StatusBar(state)
    CandidatePanel(state)
  }
}

private fun handleKeyEvent(
    event: KeyEvent,
    state: PuzzleUiState,
): Boolean {
  if (event.ctrl || event.alt) return false
  return handleNavigation(event.key, state) || handleEditing(event.key, state)
}

private fun handleNavigation(key: String, state: PuzzleUiState): Boolean {
  when (key) {
    "Escape" -> state.dismissCandidates()
    "ArrowUp" -> state.moveCursor(-1, 0)
    "ArrowDown" -> state.moveCursor(1, 0)
    "ArrowLeft" -> state.moveCursor(0, -1)
    "ArrowRight" -> state.moveCursor(0, 1)
    "Tab" -> state.toggleCandidates()
    "Enter" -> state.moveCursor(1, 0)
    else -> return false
  }
  return true
}

private fun handleEditing(key: String, state: PuzzleUiState): Boolean =
    when {
      key == "Backspace" || key == "Delete" -> {
        state.deleteLetter()
        true
      }
      key.length == 1 && key[0].isLetter() -> {
        state.typeLetter(key[0].uppercaseChar())
        true
      }
      else -> false
    }
