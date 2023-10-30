package io.github.pitonite.exch_cx.utils

import io.github.pitonite.exch_cx.model.api.OrderState
import io.github.pitonite.exch_cx.model.api.RateFeeMode
import java.math.BigDecimal
import javax.annotation.concurrent.Immutable

@Immutable
data class ParsedOrder(
    val orderid: String,
    val status: OrderState,
    val fromCurrency: String,
    val toCurrency: String,
    val rateFeeMode: RateFeeMode,
    val rate: BigDecimal,
    val networkFee: BigDecimal?,
    val calculatedFromAmount: BigDecimal?,
    val calculatedToAmount: BigDecimal?,
    val fromAddress: String?,
    val toAddress: String?,
)

object ExchParser {

  private val orderParseRegex =
      """ORDER.+?>(?<orderid>[^<]+?)<[\s\S]+?Status.+?>(?<status>[^<]+?)<[\s\S]+?Exchange rate.+?>(?<rate>[^<]+?)<[\s\S]+?rate mode.+?>(?<rateMode>[^<]+?)<(?:[\s\S]+?fee.+?>(?<fee>[^<]+?)<)?(?:[\s\S]+?Send amount.+?>(?<fromAmount>[^<]+?)<)?(?:[\s\S]+?Receive amount.+?>(?<toAmount>[^<]+?)<)?(?:[\s\S]+?address.+?>(?<toAddress>[^<]+?)<)?(?:[\s\S]+?address.+?>[^>]+?>(?<fromAddress>[^<]+?)<)?"""
          .toRegex(
              setOf(
                  RegexOption.MULTILINE,
              ))

  private val orderRateRegex =
      """[\d.]+\s*(?<fromCurrency>\w+)\s*=\s*(?<rateAmount>[\d.]+)\s*(?<toCurrency>\w+)""".toRegex()

  private val amountRegex = """(?<amount>[\d.]+)""".toRegex()

  fun parseOrder(pageContent: String): ParsedOrder? {
    runCatching {
      val result = orderParseRegex.find(pageContent)
      if (result != null) {
        val orderid = result.groups["orderid"]!!.value.trim()
        val status = OrderState.valueOf(result.groups["status"]!!.value.replace(' ', '_').trim())
        val rateFeeMode =
            RateFeeMode.valueOf(
                result.groups["rateMode"]!!.value.uppercase().replace(' ', '_').trim())
        val rateParts = orderRateRegex.find(result.groups["rate"]!!.value.trim())!!
        val fromCurrency = rateParts.groups["fromCurrency"]!!.value!!
        val toCurrency = rateParts.groups["toCurrency"]!!.value!!
        val rate = rateParts.groups["rateAmount"]!!.value!!.toBigDecimal()

        // optional parameters
        val networkFee =
            result.groups["fee"]
                ?.value
                .toString()
                .let { amountRegex.find(it)?.groups?.get("amount")?.value }
                ?.toBigDecimalOrNull()

        val calculatedFromAmount =
            result.groups["fromAmount"]?.value.toString().let {
              amountRegex.find(it)?.groups?.get("amount")?.value?.toBigDecimalOrNull()
            }
        val calculatedToAmount =
            result.groups["toAmount"]?.value.toString().let {
              amountRegex.find(it)?.groups?.get("amount")?.value?.toBigDecimalOrNull()
            }

        val fromAddress = result.groups["fromAddress"]?.value
        val toAddress = result.groups["toAddress"]?.value

        return ParsedOrder(
            orderid = orderid,
            status = status,
            rate = rate,
            rateFeeMode = rateFeeMode,
            fromCurrency = fromCurrency,
            toCurrency = toCurrency,
            networkFee = networkFee,
            calculatedFromAmount = calculatedFromAmount,
            calculatedToAmount = calculatedToAmount,
            fromAddress = fromAddress,
            toAddress = toAddress,
        )
      }
    }
    return null
  }
}
