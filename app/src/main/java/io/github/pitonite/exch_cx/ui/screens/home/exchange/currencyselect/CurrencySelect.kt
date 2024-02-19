package io.github.pitonite.exch_cx.ui.screens.home.exchange.currencyselect

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.InlineTextContent
import androidx.compose.foundation.text.appendInlineContent
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SearchBar
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.Placeholder
import androidx.compose.ui.text.PlaceholderVerticalAlign
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.pitonite.exch_cx.R
import io.github.pitonite.exch_cx.model.CurrencyDetail
import io.github.pitonite.exch_cx.ui.components.Card
import io.github.pitonite.exch_cx.ui.components.SnackbarManager
import io.github.pitonite.exch_cx.ui.components.UpBtn
import java.math.BigDecimal
import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toPersistentList

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun CurrencySelect(
    modifier: Modifier = Modifier,
    topBar: @Composable () -> Unit = {},
    aboveSearch: @Composable () -> Unit = {},
    currencyList: PersistentList<CurrencyDetail>,
    onCurrencySelected: (CurrencyDetail) -> Unit,
    showReserves: Boolean = false,
) {
  var query by remember { mutableStateOf("") }

  val visibleItems by remember {
    derivedStateOf {
      (if (query.isNotEmpty()) currencyList.filter { it.name.contains(query) }.toPersistentList()
      else currencyList)
    }
  }

  Scaffold(
      snackbarHost = { SnackbarHost(hostState = SnackbarManager.snackbarHostState) },
      topBar = topBar,
      modifier = modifier,
  ) { paddingValues ->
    Column(
        Modifier.padding(paddingValues),
    ) {
      aboveSearch()
      SearchBar(
          modifier =
              Modifier.fillMaxWidth().padding(horizontal = dimensionResource(R.dimen.page_padding)),
          query = query,
          onSearch = {},
          shape = MaterialTheme.shapes.small,
          colors =
              SearchBarDefaults.colors(
                  containerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
              ),
          onQueryChange = { query = it.lowercase() },
          active = false,
          onActiveChange = {},
          placeholder = { Text(text = stringResource(R.string.label_search)) },
          leadingIcon = {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = null,
            )
          },
          tonalElevation = 0.dp,
      ) {}
      LazyColumn(
          Modifier.padding(horizontal = dimensionResource(R.dimen.page_padding))
              .padding(top = dimensionResource(R.dimen.page_padding)),
          verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.page_padding)),
      ) {
        if (currencyList.isEmpty()) {
          item {
            Card {
              Column(
                  Modifier.padding(
                      horizontal = dimensionResource(R.dimen.padding_md),
                      vertical = 70.dp,
                  ),
                  horizontalAlignment = Alignment.CenterHorizontally,
              ) {
                Text(
                    stringResource(R.string.notice_empty_currency_list),
                    fontSize = 18.sp,
                    textAlign = TextAlign.Center,
                )
                Spacer(Modifier.height(20.dp))
                val hintText = buildAnnotatedString {
                  val refreshHint = stringResource(R.string.notice_empty_currency_list_refresh)
                  withStyle(
                      SpanStyle(
                          fontSize = 18.sp,
                      )) {
                        append(refreshHint)
                        append(" (")
                        appendInlineContent("icon", "([refresh icon])")
                        append(")")
                      }
                }
                Text(
                    text = hintText,
                    fontSize = 18.sp,
                    textAlign = TextAlign.Center,
                    inlineContent =
                        mapOf(
                            Pair(
                                "icon",
                                InlineTextContent(
                                    Placeholder(
                                        width = 18.sp,
                                        height = 18.sp,
                                        placeholderVerticalAlign = PlaceholderVerticalAlign.Center,
                                    ),
                                ) {
                                  Icon(
                                      Icons.Default.Refresh,
                                      contentDescription = stringResource(R.string.refresh),
                                  )
                                },
                            ),
                        ),
                )
              }
            }
          }
        }
        items(items = visibleItems, key = { i -> i.name }) { currency ->
          CurrencySelectItem(
              modifier = Modifier.animateItemPlacement(),
              showReserves = showReserves,
              currency = currency,
              onClick = { onCurrencySelected(currency) },
          )
        }
        item { Spacer(Modifier) }
      }
    }
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview("default")
@Composable
fun CurrencySelectPreview() {
  Surface {
    CurrencySelect(
        topBar = {
          TopAppBar(
              title = { Text("Custom Title") },
              navigationIcon = { UpBtn {} },
          )
        },
        showReserves = true,
        onCurrencySelected = {},
        currencyList =
            persistentListOf(
                CurrencyDetail("btc", reserve = BigDecimal.ONE),
                CurrencyDetail("eth", reserve = BigDecimal.TEN),
            ),
        aboveSearch = {},
    )
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview("empty")
@Composable
fun CurrencySelectEmptyPreview() {
  Surface {
    CurrencySelect(
        topBar = {
          TopAppBar(
              title = { Text("Custom Title") },
              navigationIcon = { UpBtn {} },
          )
        },
        showReserves = true,
        onCurrencySelected = {},
        currencyList = persistentListOf(),
        aboveSearch = {},
    )
  }
}

enum class CurrencySelection {
  FROM,
  TO
}
