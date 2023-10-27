package io.github.pitonite.exch_cx.ui.screens.home.exchange.currencyselect

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SearchBar
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.github.pitonite.exch_cx.R
import io.github.pitonite.exch_cx.data.RateFeeRepositoryMock
import io.github.pitonite.exch_cx.ui.components.UpBtn
import io.github.pitonite.exch_cx.ui.screens.home.exchange.ExchangeViewModel
import io.github.pitonite.exch_cx.ui.theme.ExchTheme

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun CurrencySelect(
    exchangeViewModel: ExchangeViewModel,
    viewModel: CurrencySelectViewModel,
    upPress: () -> Unit,
    modifier: Modifier = Modifier,
    currencySelection: CurrencySelection = CurrencySelection.FROM,
) {
  val currencyList by viewModel.currencyListState.collectAsStateWithLifecycle()

  Column {
    TopAppBar(
        title = {
          Text(
              if (currencySelection == CurrencySelection.FROM) stringResource(R.string.you_pay)
              else stringResource(R.string.you_receive))
        },
        navigationIcon = { UpBtn(upPress) },
    )
    SearchBar(
        modifier =
            Modifier.fillMaxWidth().padding(horizontal = dimensionResource(R.dimen.page_padding)),
        query = viewModel.searchTerm,
        onSearch = {},
        shape = MaterialTheme.shapes.small,
        colors =
            SearchBarDefaults.colors(
                containerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
            ),
        onQueryChange = { viewModel.updateSearchTerm(it) },
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
        modifier.padding(dimensionResource(R.dimen.page_padding)),
        verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.page_padding)),
    ) {
      items(items = currencyList, key = { i -> i.name }) { currency ->
        CurrencySelectItem(
            modifier = Modifier.animateItemPlacement(),
            currency = currency,
            onClick = {
              if (currencySelection == CurrencySelection.FROM) {
                exchangeViewModel.updateFromCurrency(currency.name)
              } else {
                exchangeViewModel.updateToCurrency(currency.name)
              }
              upPress()
            },
        )
      }
    }
  }
}

@Preview("default")
@Preview("large font", fontScale = 2f)
@Composable
fun CurrencySelectPreview() {
  ExchTheme {
    CurrencySelect(
        viewModel =
            CurrencySelectViewModel(
                SavedStateHandle(),
                RateFeeRepositoryMock(),
            ),
        upPress = {},
        exchangeViewModel =
            ExchangeViewModel(
                SavedStateHandle(),
                RateFeeRepositoryMock(),
            ))
  }
}

enum class CurrencySelection {
  FROM,
  TO
}
