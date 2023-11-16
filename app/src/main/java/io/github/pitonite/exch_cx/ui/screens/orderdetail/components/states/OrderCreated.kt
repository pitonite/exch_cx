package io.github.pitonite.exch_cx.ui.screens.orderdetail.components.states

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import io.github.pitonite.exch_cx.R
import io.github.pitonite.exch_cx.data.OrderRepositoryMock.Companion.orderCreated
import io.github.pitonite.exch_cx.data.OrderRepositoryMock.Companion.orderCreatedToAddressInvalid
import io.github.pitonite.exch_cx.data.room.Order
import io.github.pitonite.exch_cx.model.api.OrderStateError
import io.github.pitonite.exch_cx.ui.screens.orderdetail.components.OrderStateCard
import io.github.pitonite.exch_cx.ui.theme.ExchTheme
import io.github.pitonite.exch_cx.utils.WorkState
import io.github.pitonite.exch_cx.utils.isWorking

@Composable
fun OrderCreated(
    order: Order,
    onSubmitNewToAddress: (String) -> Unit = {},
    submitWorkState: WorkState = WorkState.NotWorking,
) {
  OrderStateCard() {
    val focusManager = LocalFocusManager.current

    if (order.stateError?.knownOrNull() == OrderStateError.TO_ADDRESS_INVALID) {
      Text(
          modifier = Modifier.fillMaxWidth(),
          text =
              stringResource(
                  R.string.order_error_to_address_invalid_title, order.toCurrency.uppercase()),
          style = MaterialTheme.typography.headlineSmall,
          textAlign = TextAlign.Center)

      var newAddress by rememberSaveable(key = "newAddress_${order.id}") { mutableStateOf("") }
      OutlinedTextField(
          modifier = Modifier.fillMaxWidth(),
          value = newAddress,
          onValueChange = { newAddress = it },
          label = {
            Text(stringResource(R.string.label_new_to_address, order.toCurrency.uppercase()))
          },
          keyboardActions =
              KeyboardActions(
                  onDone = { focusManager.clearFocus() },
              ),
          keyboardOptions =
              KeyboardOptions(keyboardType = KeyboardType.Text, imeAction = ImeAction.Done),
      )
      Row(modifier = Modifier.fillMaxWidth(),horizontalArrangement = Arrangement.Center) {
        Button(
            onClick = { onSubmitNewToAddress(newAddress) },
            enabled = newAddress.isNotEmpty() && !submitWorkState.isWorking()) {
              if (submitWorkState.isWorking()) {
                CircularProgressIndicator()
              } else {
                Text(stringResource(R.string.submit))
              }
            }
      }
    } else {
      Text(
          text = stringResource(R.string.order_state_created_title),
          style = MaterialTheme.typography.headlineSmall)

      Spacer(Modifier.padding(top = dimensionResource(R.dimen.padding_xl)))

      Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
        CircularProgressIndicator()
      }

      Spacer(Modifier.padding(top = dimensionResource(R.dimen.padding_xl)))
    }
  }
}

@Preview
@Composable
fun OrderCreatedPreview() {
  ExchTheme { Surface() { OrderCreated(orderCreated) } }
}

@Preview
@Composable
fun OrderCreatedToAddressInvalidPreview() {
  ExchTheme { Surface() { OrderCreated(orderCreatedToAddressInvalid) } }
}
