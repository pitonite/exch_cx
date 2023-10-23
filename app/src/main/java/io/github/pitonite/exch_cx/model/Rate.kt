package io.github.pitonite.exch_cx.model

import androidx.compose.runtime.Immutable
import io.github.pitonite.exch_cx.utils.BigDecimalSerializer
import kotlinx.serialization.Serializable
import nl.adaptivity.xmlutil.serialization.XmlElement
import nl.adaptivity.xmlutil.serialization.XmlSerialName
import java.math.BigDecimal

// for rates.xml

@Serializable
@Immutable
data class Rate(
    @XmlElement(true) @XmlSerialName("from", "", "") val fromCurrency: String,
    @XmlElement(true) @XmlSerialName("to", "", "") val toCurrency: String,

    /** the amount of the [fromCurrency] Must be given to receive [outAmount] of [toCurrency] */
    @Serializable(with = BigDecimalSerializer::class)
    @XmlElement(true)
    @XmlSerialName("in", "", "")
    val inAmount: BigDecimal,

    /** the amount that will be received for the given [inAmount] of [fromCurrency] */
    @Serializable(with = BigDecimalSerializer::class)
    @XmlElement(true)
    @XmlSerialName("out", "", "")
    val outAmount: BigDecimal,

    /**
     * This is the maximum available amount of [toCurrency]. user cannot receive higher than this
     * amount (in other words, [outAmount] cannot be higher than this).
     */
    @Serializable(with = BigDecimalSerializer::class)
    @XmlElement(true)
    @XmlSerialName("amount", "", "")
    val availableAmount: BigDecimal,

    /** This is the required minimum amount of [fromCurrency] */
    @Serializable(with = BigDecimalSerializer::class)
    @XmlElement(true)
    @XmlSerialName("minamount", "", "")
    val minAmount: BigDecimal,

    /** This is the maximum amount of [fromCurrency]. user cannot request more than this amount. */
    @Serializable(with = BigDecimalSerializer::class)
    @XmlElement(true)
    @XmlSerialName("maxamount", "", "")
    val maxAmount: BigDecimal,
)

@Serializable
@XmlSerialName("rates")
@Immutable
data class Rates(
    @XmlSerialName("item") val rates: List<Rate>,
)
