package io.github.pitonite.exch_cx.ui.screens.orderdetail.components.states

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.pitonite.exch_cx.R
import io.github.pitonite.exch_cx.data.OrderRepositoryMock.Companion.orderAwaitingInput
import io.github.pitonite.exch_cx.data.OrderRepositoryMock.Companion.orderAwaitingInputMaxInputZero
import io.github.pitonite.exch_cx.data.OrderRepositoryMock.Companion.orderAwaitingInputWithFromAmount
import io.github.pitonite.exch_cx.data.room.GENERATING_FROM_ADDRESS
import io.github.pitonite.exch_cx.data.room.Order
import io.github.pitonite.exch_cx.ui.components.CopyableText
import io.github.pitonite.exch_cx.ui.components.Notice
import io.github.pitonite.exch_cx.ui.components.TextWithLoading
import io.github.pitonite.exch_cx.ui.screens.orderdetail.components.OrderStateCard
import io.github.pitonite.exch_cx.ui.screens.orderdetail.etheriumBasedCoins
import io.github.pitonite.exch_cx.ui.theme.ExchTheme
import io.github.pitonite.exch_cx.utils.rememberQrBitmapPainter
import java.math.BigDecimal

@Composable
fun OrderAwaitingInput(
    order: Order,
) {
  OrderStateCard {
    if (order.maxInput.compareTo(BigDecimal.ZERO) == 0) {

      Text(stringResource(R.string.order_state_max_input_zero, order.fromCurrency))
    } else {
      Text(
          stringResource(R.string.order_state_waiting_for_input_title, order.fromCurrency),
          style = MaterialTheme.typography.headlineSmall,
      )

      Spacer(Modifier)

      if (order.fromAmount != null) {
        SelectionContainer {
          Text(stringResource(R.string.label_send, order.fromAmount, order.fromCurrency))
        }
        SelectionContainer {
          val hundred = BigDecimal.valueOf(100)
          val receiveAmount =
              order.fromAmount
                  .times(
                      BigDecimal.valueOf(100).minus(order.svcFee).divide(hundred).times(order.rate),
                  )
                  .minus(order.networkFee ?: BigDecimal.ZERO)
                  .stripTrailingZeros()
          Text(
              stringResource(R.string.label_receive, receiveAmount, order.toCurrency) +
                  " (${stringResource(R.string.network_fee_included)})",
          )
        }
      } else {
        SelectionContainer {
          Text(stringResource(R.string.label_minimum, order.minInput, order.fromCurrency))
        }

        SelectionContainer {
          Text(stringResource(R.string.label_maximum, order.maxInput, order.fromCurrency))
        }
      }

      Spacer(Modifier)

      Column(verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.padding_md))) {
        Text(stringResource(R.string.label_to_order_address, order.fromCurrency))

        if (order.fromAddr != GENERATING_FROM_ADDRESS) {
          CopyableText(
              order.fromAddr,
              copyConfirmationMessage = R.string.snack_address_copied,
              fontSize = 19.sp,
          )

          if (order.stateError == null && etheriumBasedCoins.containsMatchIn(order.fromCurrency)) {
            Notice(stringResource(R.string.notice_etherium_based_coins))
          }

          Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.Center,
          ) {
            Image(
                painter =
                    rememberQrBitmapPainter(
                        content = order.fromAddr,
                        size = 300.dp,
                        padding = 1.dp,
                    ),
                contentDescription =
                    stringResource(R.string.desc_send_qrcode_image, order.fromCurrency),
                modifier = Modifier.clip(MaterialTheme.shapes.large),
            )
          }

          if (order.fromAmount != null) {
            Notice {
              Text(
                  stringResource(
                      R.string.notice_order_different_amount,
                      order.fromAmount,
                      order.fromCurrency,
                  ),
                  textAlign = TextAlign.Justify,
              )

              SelectionContainer {
                Text(stringResource(R.string.label_minimum, order.minInput, order.fromCurrency))
              }

              SelectionContainer {
                Text(stringResource(R.string.label_maximum, order.maxInput, order.fromCurrency))
              }
            }
          }
        } else {
          TextWithLoading(stringResource(R.string.address_generating), fontSize = MaterialTheme.typography.bodyLarge.fontSize,color = MaterialTheme.colorScheme.onSurfaceVariant )
        }
      }
    }
  }
}

@Preview
@Composable
fun OrderAwaitingInputPreview() {
  ExchTheme { Surface() { OrderAwaitingInput(orderAwaitingInput) } }
}

@Preview
@Composable
fun OrderAwaitingInputWithFromAmountPreview() {
  ExchTheme { Surface() { OrderAwaitingInput(orderAwaitingInputWithFromAmount) } }
}

@Preview
@Composable
fun OrderAwaitingInputMaxInputZeroPreview() {
  ExchTheme { Surface() { OrderAwaitingInput(orderAwaitingInputMaxInputZero) } }
}
