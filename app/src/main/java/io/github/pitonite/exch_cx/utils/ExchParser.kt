package io.github.pitonite.exch_cx.utils

import androidx.compose.runtime.Stable
import io.github.pitonite.exch_cx.model.api.OrderState
import io.github.pitonite.exch_cx.model.api.RateFeeMode
import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.toPersistentList
import java.math.BigDecimal

@Stable
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

@Stable
data class ParsedRate(
    val fromCurrency: String,
    val toCurrency: String,
    val rate: BigDecimal,
)

@Stable
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

  private val rateParser =
      """col-5 text-end text-muted.+?>\s*(\w+)\s*<[\s\S]+?start"\s*>\s*([\d.]+)""".toRegex()

  private val homeErrorParser = """role="alert"[^>]*?>[^>]*?strong>([\w\s]+)<\/strong""".toRegex()

  fun parseOrder(pageContent: String): ParsedOrder? {
    runCatching {
      val result = orderParseRegex.find(pageContent)
      if (result != null) {
        val orderid = result.groups["orderid"]!!.value.trim()
        val status = OrderState.valueOf(result.groups["status"]!!.value.trim().replace(' ', '_'))
        val rateFeeMode =
            RateFeeMode.valueOf(
                result.groups["rateMode"]!!.value.uppercase().trim().replace(' ', '_'))
        val rateParts = orderRateRegex.find(result.groups["rate"]!!.value.trim())!!
        val fromCurrency = rateParts.groups["fromCurrency"]!!.value.trim()
        val toCurrency = rateParts.groups["toCurrency"]!!.value.trim()
        val rate = rateParts.groups["rateAmount"]!!.value.trim().toBigDecimal()

        // optional parameters
        val networkFee =
            result.groups["fee"]
                ?.value
                ?.let { amountRegex.find(it.trim())?.groups?.get("amount")?.value?.trim() }
                ?.toBigDecimalOrNull()

        val calculatedFromAmount =
            result.groups["fromAmount"]?.value.toString().let {
              amountRegex.find(it)?.groups?.get("amount")?.value?.trim()?.toBigDecimalOrNull()
            }
        val calculatedToAmount =
            result.groups["toAmount"]?.value?.trim()?.let {
              amountRegex.find(it)?.groups?.get("amount")?.value?.trim()?.toBigDecimalOrNull()
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

  fun parseRates(homePageHtml: String): PersistentList<ParsedRate> {
    val rates = mutableListOf<ParsedRate>()

    val matches = rateParser.findAll(homePageHtml)

    for (match in matches) {
      if (!match.groups.isNullOrEmpty()) {
        val (fromCurrency, toCurrency) =
            match.groups[1]!!.value.trim().split('_').map { it.lowercase() }
        val rate = match.groups[2]!!.value.trim().toBigDecimal()
        rates.add(ParsedRate(fromCurrency, toCurrency, rate))
      }
    }

    return rates.toPersistentList()
  }

  fun parseError(homePageHtml: String): String? {
    return homeErrorParser.find(homePageHtml)?.groups?.get(1)?.value?.trim()
  }
}
