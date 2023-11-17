package io.github.pitonite.exch_cx.ui.screens.ordersupport

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemKey
import io.github.pitonite.exch_cx.R
import io.github.pitonite.exch_cx.data.SupportMessagesRepositoryMock
import io.github.pitonite.exch_cx.data.room.SupportMessage
import io.github.pitonite.exch_cx.exceptions.toUserMessage
import io.github.pitonite.exch_cx.model.SnackbarMessage
import io.github.pitonite.exch_cx.ui.components.Card
import io.github.pitonite.exch_cx.ui.components.RefreshButton
import io.github.pitonite.exch_cx.ui.components.SnackbarManager
import io.github.pitonite.exch_cx.ui.navigation.NavArgs
import io.github.pitonite.exch_cx.ui.screens.ordersupport.components.JumpToBottom
import io.github.pitonite.exch_cx.ui.screens.ordersupport.components.MessageItem
import io.github.pitonite.exch_cx.ui.screens.ordersupport.components.UserInput
import io.github.pitonite.exch_cx.ui.theme.ExchTheme
import io.github.pitonite.exch_cx.utils.isWorking
import io.github.pitonite.exch_cx.utils.noRippleClickable
import io.github.pitonite.exch_cx.utils.verticalFadingEdge
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrderSupport(
  viewModel: OrderSupportViewModel,
  upPress: () -> Unit,
  modifier: Modifier = Modifier
) {
  val orderid by viewModel.orderid.collectAsStateWithLifecycle()
  val messages = viewModel.messagesPagingDataFlow.collectAsLazyPagingItems()
  val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior(rememberTopAppBarState())
  val scrollState = rememberLazyListState()
  val focusManager = LocalFocusManager.current
  val scope = rememberCoroutineScope()

  LaunchedEffect(key1 = messages.loadState.refresh) {
    if (messages.loadState.refresh is LoadState.Error) {
      SnackbarManager.showMessage(
          SnackbarMessage.from(
              message = (messages.loadState.refresh as LoadState.Error).error.toUserMessage(),
          ),
      )
    }
  }

  LaunchedEffect(true) {
    // You can replace this with lifecycleOwner.repeatOnLifecycle(Lifecycle.State.RESUMED) if
    // needed
    coroutineScope {
      while (isActive) {
        viewModel.refreshMessages()
        delay(30000L)
      }
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
                scrolledContainerColor = MaterialTheme.colorScheme.inverseOnSurface,
            ),
            title = {
              Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(stringResource(R.string.support_chat))
                SelectionContainer {
                  Text(
                      orderid,
                      color = MaterialTheme.colorScheme.primary,
                      fontSize = 16.sp,
                  )
                }
              }
            },
            navigationIcon = {
              IconButton(onClick = { upPress() }) {
                Icon(
                    modifier = Modifier.size(32.dp),
                    imageVector = Icons.AutoMirrored.Default.ArrowBack,
                    contentDescription = null,
                )
              }
            },
            actions = {
              if (orderid.isNotEmpty()) {
                RefreshButton(
                    onClick = { viewModel.refreshMessages() },
                    enabled = !viewModel.refreshWorkState.isWorking(),
                    refreshing = viewModel.refreshWorkState.isWorking(),
                )
              }
            },
        )
      },
  ) { padding ->
    Column(
        modifier.fillMaxSize()
            .padding(padding)
            .noRippleClickable { focusManager.clearFocus() },
    ) {
      Messages(
          messages = messages,
          scrollState = scrollState,
          modifier = Modifier
              .weight(1f, true).fillMaxWidth(),
      )
      UserInput(
          value = viewModel.messageDraft,
          onValueChanged = viewModel::updateMessageDraft,
          onSendMessage = {
            viewModel.sendMessage {
              scope.launch {
                scrollState.scrollToItem(0)
              }
            }
          },
          sendingMessage = viewModel.sendingWorkState.isWorking(),
      )
    }
  }

}

fun areSameDay(timeInMillis1: Long, timeInMillis2: Long): Boolean {
  val calendar1 = Calendar.getInstance().apply { timeInMillis = timeInMillis1 }
  val calendar2 = Calendar.getInstance().apply { timeInMillis = timeInMillis2 }

  return calendar1.get(Calendar.YEAR) == calendar2.get(Calendar.YEAR) &&
      calendar1.get(Calendar.DAY_OF_YEAR) == calendar2.get(Calendar.DAY_OF_YEAR)
}

@Composable
private fun Messages(
  messages: LazyPagingItems<SupportMessage>,
  scrollState: LazyListState,
  modifier: Modifier = Modifier
) {
  val scope = rememberCoroutineScope()
  Box(modifier = modifier) {
    when (messages.loadState.refresh) {

      is LoadState.Error ->
        Card {
          Column(Modifier.padding(vertical = 70.dp, horizontal = 20.dp)) {
            Text(stringResource(R.string.unknown_error))
          }
        }

      else -> {
        LazyColumn(
            reverseLayout = true,
            state = scrollState,
            modifier = Modifier
                .fillMaxSize()
                .verticalFadingEdge(scrollState, dimensionResource(R.dimen.fading_edge)),
            horizontalAlignment = Alignment.CenterHorizontally,

            ) {

          if (messages.itemCount == 0) {
            if (messages.loadState.refresh is LoadState.Loading) {
              item {
                CircularProgressIndicator(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .size(50.dp),
                )
              }
            } else {
              item {
                Column(
                    Modifier
                        .fillParentMaxHeight()
                        .padding(dimensionResource(R.dimen.page_padding)),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                ) {

                  Surface(
                      color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                      shape = MaterialTheme.shapes.large,
                  ) {
                    Text(
                        text = stringResource(R.string.notice_empty_support_chat),
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.padding(
                            horizontal = dimensionResource(R.dimen.padding_md),
                            vertical = dimensionResource(R.dimen.padding_xs),
                        ),
                    )
                  }
                }
              }
            }

          } else {
            item {
              Spacer(Modifier.height(8.dp))
            }
          }
          items(
              count = messages.itemCount,
              key = messages.itemKey { it.index },
          ) { index ->
            val item = messages[index]
            if (item != null) {
              // in reverse mode lazy loading, next item is prev message actually
              // so we are seeing last message first
              // so rendering is a bit tricky
              val nextItem = if (index > 0) messages[index - 1] else null
              val prevItem = if (index < messages.itemCount - 1) messages[index + 1] else null
              val shouldUngroupMessage = prevItem?.sender != item.sender || prevItem?.let {
                areSameDay(
                    item.createdAt.time,
                    it.createdAt.time,
                )
              } == false
              MessageItem(item, shouldUngroupMessage)

              if (nextItem?.sender != item.sender) {
                Spacer(Modifier.height(dimensionResource(R.dimen.padding_md)))
              } else {
                Spacer(Modifier.height(dimensionResource(R.dimen.padding_xs)))
              }
            }
          }
          item {
            if (messages.loadState.append == LoadState.Loading) {
              CircularProgressIndicator(modifier = Modifier.padding(16.dp))
            }
          }
        }
      }
    }

    // Jump to bottom button shows up when user scrolls past a threshold.
    // Convert to pixels:
    val jumpThreshold = with(LocalDensity.current) {
      jumpToBottomThreshold.toPx()
    }

    // Show the button if the first visible item is not the first one or if the offset is
    // greater than the threshold.
    val jumpToBottomButtonEnabled by remember {
      derivedStateOf {
        scrollState.firstVisibleItemIndex != 0 ||
            scrollState.firstVisibleItemScrollOffset > jumpThreshold
      }
    }

    JumpToBottom(
        // Only show if the scroller is not at the bottom
        enabled = jumpToBottomButtonEnabled,
        onClicked = {
          scope.launch {
            scrollState.animateScrollToItem(0)
          }
        },
        modifier = Modifier.align(Alignment.BottomCenter),
    )
  }
}


@Preview("default")
@Composable
fun OrderSupportPreview() {
  val savedStateHandle = SavedStateHandle()
  savedStateHandle[NavArgs.ORDER_ID_KEY] = "ee902b8a5fe0844d41"
  ExchTheme {
    OrderSupport(
        viewModel =
        OrderSupportViewModel(
            savedStateHandle,
            SupportMessagesRepositoryMock(),
        ),
        upPress = {},
    )
  }
}

private val jumpToBottomThreshold = 56.dp
