package io.github.pitonite.exch_cx.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ArrowDropUp
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import kotlinx.collections.immutable.PersistentList

const val ALPHA_FULL = 1f
const val ALPHA_DISABLED = 0.6f

// from https://proandroiddev.com/improving-the-compose-dropdownmenu-88469b1ef34
// with edits

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun <T> LargeDropdownMenu(
    modifier: Modifier = Modifier,
    items: PersistentList<T>,
    selectedIndex: Int = -1,
    selectedItemToString: (T) -> String = { it.toString() },
    enabled: Boolean = true,
    label: String = "",
    trigger: @Composable (expanded: Boolean) -> Unit = { expanded ->
      OutlinedTextField(
          label = { Text(label) },
          value = items.getOrNull(selectedIndex)?.let { selectedItemToString(it) } ?: "",
          enabled = enabled,
          modifier = Modifier.fillMaxWidth(),
          trailingIcon = {
            val icon = if (expanded) Icons.Filled.ArrowDropUp else Icons.Filled.ArrowDropDown
            Icon(icon, "")
          },
          onValueChange = {},
          readOnly = true,
      )
    },
    onItemSelected: (index: Int, item: T) -> Unit,
    drawItem: @Composable (T, Boolean, Boolean, () -> Unit) -> Unit =
        { item, selected, itemEnabled, onClick ->
          LargeDropdownMenuItem(
              text = item.toString(),
              selected = selected,
              enabled = itemEnabled,
              onClick = onClick,
          )
        },
) {
  var expanded by remember { mutableStateOf(false) }

  Box(
      modifier = modifier.width(IntrinsicSize.Min).height(IntrinsicSize.Min),
      contentAlignment = Alignment.Center,
  ) {
    trigger(expanded)
    // Transparent clickable surface on top of OutlinedTextField
    Surface(
        modifier =
            Modifier.fillMaxSize().clip(MaterialTheme.shapes.extraSmall).clickable(
                enabled = enabled) {
                  expanded = true
                },
        color = Color.Transparent,
    ) {}
  }

  if (expanded) {
    BasicAlertDialog(
        onDismissRequest = { expanded = false },
    ) {
      Surface(
          shape = RoundedCornerShape(12.dp),
      ) {
        val listState = rememberLazyListState()
        if (selectedIndex > -1) {
          LaunchedEffect("ScrollToSelected") { listState.scrollToItem(index = selectedIndex) }
        }

        LazyColumn(modifier = Modifier.fillMaxWidth(), state = listState) {
          itemsIndexed(items) { index, item ->
            val selectedItem = index == selectedIndex
            drawItem(item, selectedItem, true) {
              onItemSelected(index, item)
              expanded = false
            }

            if (index < items.lastIndex) {
              HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
            }
          }
        }
      }
    }
  }
}

@Composable
fun LargeDropdownMenuItem(
    text: String,
    selected: Boolean,
    enabled: Boolean,
    onClick: () -> Unit,
) {
  val contentColor =
      when {
        !enabled -> MaterialTheme.colorScheme.onSurface.copy(alpha = ALPHA_DISABLED)
        selected -> MaterialTheme.colorScheme.primary.copy(alpha = ALPHA_FULL)
        else -> MaterialTheme.colorScheme.onSurface.copy(alpha = ALPHA_FULL)
      }

  CompositionLocalProvider(LocalContentColor provides contentColor) {
    Box(modifier = Modifier.clickable(enabled) { onClick() }.fillMaxWidth().padding(16.dp)) {
      Text(
          text = text,
          style = MaterialTheme.typography.titleSmall,
      )
    }
  }
}
