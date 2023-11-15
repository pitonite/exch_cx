package io.github.pitonite.exch_cx.ui.screens.ordersupport

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.SavedStateHandle
import io.github.pitonite.exch_cx.data.SupportMessagesRepositoryMock
import io.github.pitonite.exch_cx.ui.navigation.NavArgs
import io.github.pitonite.exch_cx.ui.theme.ExchTheme

@Composable
fun OrderSupport(
    viewModel: OrderSupportViewModel,
    upPress: () -> Unit,
    modifier: Modifier = Modifier
) {
  // TODO
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
