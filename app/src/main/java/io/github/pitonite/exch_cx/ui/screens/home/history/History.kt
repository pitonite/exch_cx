package io.github.pitonite.exch_cx.ui.screens.home.history

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.exclude
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScaffoldDefaults
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.SavedStateHandle
import androidx.paging.LoadState
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemKey
import io.github.pitonite.exch_cx.R
import io.github.pitonite.exch_cx.data.OrderRepositoryMock
import io.github.pitonite.exch_cx.model.SnackbarMessage
import io.github.pitonite.exch_cx.model.UserMessage
import io.github.pitonite.exch_cx.ui.components.Card
import io.github.pitonite.exch_cx.ui.components.SnackbarManager
import io.github.pitonite.exch_cx.ui.screens.home.orders.OrderItem
import io.github.pitonite.exch_cx.ui.theme.ExchTheme
import io.github.pitonite.exch_cx.utils.verticalFadingEdge

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun History(
    viewModel: HistoryViewModel,
    onOrderSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
  val orderPagingItems = viewModel.orderPagingDataFlow.collectAsLazyPagingItems()
  val listState = rememberLazyListState()
  val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior(rememberTopAppBarState())

  LaunchedEffect(key1 = orderPagingItems.loadState.refresh) {
    if (orderPagingItems.loadState.refresh is LoadState.Error) {
      val errorMsg = (orderPagingItems.loadState.refresh as LoadState.Error).error.localizedMessage
      SnackbarManager.showMessage(
          SnackbarMessage.from(
              message =
                  if (errorMsg.isNullOrEmpty()) UserMessage.from(R.string.unknown_error)
                  else UserMessage.from(errorMsg),
          ),
      )
    }
  }

  Scaffold(
      modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
      snackbarHost = { SnackbarHost(hostState = SnackbarManager.snackbarHostState) },
      topBar = {
        CenterAlignedTopAppBar(
            scrollBehavior = scrollBehavior,
            colors =
                TopAppBarDefaults.centerAlignedTopAppBarColors(
                    scrolledContainerColor = MaterialTheme.colorScheme.inverseOnSurface),
            title = { Text(stringResource(R.string.history)) },
        )
      },
      contentWindowInsets = ScaffoldDefaults
          .contentWindowInsets
          .exclude(WindowInsets.navigationBars)
  ) { padding ->
    Box(
        modifier =
            modifier
                .padding(padding)
                .padding(horizontal = dimensionResource(R.dimen.page_padding))
                .fillMaxSize(),
    ) {
      when (orderPagingItems.loadState.refresh) {
        is LoadState.Loading ->
            CircularProgressIndicator(modifier = Modifier.align(Alignment.Center).size(50.dp))
        is LoadState.Error ->
            Card {
              Column(Modifier.padding(vertical = 70.dp, horizontal = 20.dp)) {
                Text(stringResource(R.string.unknown_error))
              }
            }
        else -> {
          if (orderPagingItems.itemCount == 0) {
            Column {
              Card {
                Column(
                    Modifier.padding(
                        horizontal = dimensionResource(R.dimen.padding_md),
                        vertical = 70.dp,
                    ),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                  Text(
                      stringResource(R.string.notice_empty_history),
                      fontSize = 22.sp,
                      textAlign = TextAlign.Center,
                  )
                }
              }
            }
          } else {
            LazyColumn(
                state = listState,
                modifier =
                    Modifier.fillMaxSize()
                        .verticalFadingEdge(listState, dimensionResource(R.dimen.fading_edge)),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.page_padding)),
            ) {
              items(
                  count = orderPagingItems.itemCount,
                  key = orderPagingItems.itemKey { it.id },
              ) { index ->
                val order = orderPagingItems[index]
                if (order != null) {
                  OrderItem(
                      order,
                      onClick = { onOrderSelected(order.id) },
                  )
                }
              }
              item {
                if (orderPagingItems.loadState.append == LoadState.Loading) {
                  CircularProgressIndicator(modifier = Modifier.padding(16.dp))
                }
              }
            }
          }
        }
      }
    }
  }
}

@Preview("default")
@Preview("large font", fontScale = 2f)
@Composable
fun HistoryPreview() {
  ExchTheme {
    History(
        HistoryViewModel(SavedStateHandle(), OrderRepositoryMock()),
        onOrderSelected = {},
    )
  }
}
