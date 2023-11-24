package io.github.pitonite.exch_cx

import io.github.pitonite.exch_cx.data.UserSettingsRepositoryMock
import io.github.pitonite.exch_cx.network.ExchHttpClient
import io.github.pitonite.exch_cx.model.api.OrderResponse
import io.github.pitonite.exch_cx.model.api.RateFeeResponse
import io.github.pitonite.exch_cx.model.api.RateFeesObjectTransformer
import io.github.pitonite.exch_cx.model.api.XmlRateFeesResponse
import io.github.pitonite.exch_cx.model.api.SupportMessagesArrayTransformer
import io.github.pitonite.exch_cx.utils.jsonFormat
import io.github.pitonite.exch_cx.utils.xmlFormat
import io.ktor.client.call.body
import io.ktor.client.request.accept
import io.ktor.http.ContentType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.newSingleThreadContext
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test

class ParsingUnitTest {

  private val mainThreadSurrogate = newSingleThreadContext("UI thread")

  @Before
  fun setUp() {
    Dispatchers.setMain(mainThreadSurrogate)
  }

  @After
  fun tearDown() {
    Dispatchers.resetMain() // reset the main dispatcher to the original Main dispatcher
    mainThreadSurrogate.close()
  }

  @Test
  fun canParseOrder() {
    val resp =
        """{
    "created": 1696908374,
    "from_addr": "_GENERATING_",
    "from_amount_received": null,
    "from_currency": "BTC",
    "max_input": "1.58325398",
    "min_input": "0.00000055",
    "network_fee": 0,
    "orderid": "24d0d384b7400efe22",
    "rate": "180.782789478442",
    "rate_mode": "dynamic",
    "state": "CREATED",
    "state_error": "TO_ADDRESS_INVALID",
    "svc_fee": "0.500000000000000000",
    "to_address": "adasdasdasdasd",
    "to_amount": null,
    "to_currency": "XMR",
    "transaction_id_received": null,
    "transaction_id_sent": null
}"""

    val orderResponse = jsonFormat.decodeFromString(OrderResponse.serializer(), resp)

    assertNotNull(orderResponse)
  }

  @Test
  fun canParseRateFees() {
    val resp =
        """
    {
    "BTCLN_BTC": {
        "network_fee": {
            "f": "0.00006308",
            "m": "0.00005421",
            "s": "0.00000000"
        },
        "rate": "0.99009901",
        "rate_mode": "flat",
        "reserve": 43.91344041,
        "svc_fee": "1.000000000000000000"
    },
    "BTCLN_DAI": {
        "network_fee": {
            "f": 2.68581947520259,
            "m": 1.15129466460259
        },
        "rate": "26471.154600000001664739",
        "rate_mode": "flat",
        "reserve": "54687.532388354098657146",
        "svc_fee": "1.000000000000000000"
    }
}      
      """
            .trimIndent()

    val feeRates = jsonFormat.decodeFromString(RateFeesObjectTransformer, resp)

    assertNotNull(feeRates)
  }

  @Test
  fun canParseRatesXml() {

    val resp =
        """<?xml version="1.0"?>
	<rates>
				<item>
				    <from>BTC</from>
				    <to>BTCLN</to>
				    <in>1</in>
				    <out>0.99500000</out>
				    <amount>4.57248336</amount>
				    <minamount>0.00000995</minamount>
				    <maxamount>4.54973469</maxamount>
				</item>

				<item>
				    <from>BTC</from>
				    <to>USDT</to>
				    <in>1</in>
				    <out>26553.395850</out>
				    <amount>18466.233078</amount>
				    <minamount>0.00003729</minamount>
				    <maxamount>0.68851794</maxamount>
				</item>
	</rates>
      """
            .trimIndent()

    val xmlRateFeesResponse = xmlFormat.decodeFromString(XmlRateFeesResponse.serializer(), resp)

    assertNotNull(xmlRateFeesResponse)
  }

  @Test
  fun canFetchRateFee() = runBlocking {
    val client = ExchHttpClient(UserSettingsRepositoryMock())
    val resp: RateFeeResponse = client.get("/api/rates").body()

    assertNotNull(resp)
  }

  @Test
  fun canFetchXmlRateFee() = runBlocking {
    val client = ExchHttpClient(UserSettingsRepositoryMock())
    val rateFees = client
        .get("/rates.xml") {
          headers["X-Requested-With"] = "XMLHttpRequest"
          accept(ContentType.Text.Html)
        }
        .body<XmlRateFeesResponse>()
        .rateFees

    assertNotNull(rateFees)
  }

  @Test
  fun canParseSupportMessages() {
    val resp =
        """
      [
          {
              "message": " Hi ",
              "read_by_support": true,
              "sender": "USER",
              "timestamp": 1699948100
          },
          {
              "message": " Hello ",
              "sender": "SUPPORT",
              "timestamp": 1699948200
          }
      ]
    """
            .trimIndent()

    val messagesResponse = jsonFormat.decodeFromString(SupportMessagesArrayTransformer, resp)

    assertNotNull(messagesResponse)
  }
}
