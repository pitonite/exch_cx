package io.github.pitonite.exch_cx.data.mappers

import io.github.pitonite.exch_cx.data.room.Order
import io.github.pitonite.exch_cx.data.room.OrderUpdate
import io.github.pitonite.exch_cx.data.room.OrderUpdateWithArchive
import io.github.pitonite.exch_cx.model.api.OrderResponse
import java.util.Date

fun Order.toOrderUpdateEntity() =
    OrderUpdate(
        id = this.id,
        createdAt = this.createdAt,
        fromAddr = this.fromAddr,
        fromCurrency = this.fromCurrency,
        fromAmountReceived = this.fromAmountReceived,
        maxInput = this.maxInput,
        minInput = this.minInput,
        networkFee = this.networkFee,
        rate = this.rate,
        rateMode = this.rateMode,
        state = this.state,
        stateError = this.stateError,
        svcFee = this.svcFee,
        toAmount = this.toAmount,
        toAddress = this.toAddress,
        toCurrency = this.toCurrency,
        transactionIdReceived = this.transactionIdReceived,
        transactionIdSent = this.transactionIdSent,
    )

fun OrderResponse.toOrderUpdateEntity() =
    OrderUpdate(
        id = this.id,
        createdAt = Date((this.created?.times(1000)) ?: System.currentTimeMillis()),
        fromAddr = this.fromAddr,
        fromCurrency = this.fromCurrency,
        fromAmountReceived = this.fromAmountReceived,
        maxInput = this.maxInput,
        minInput = this.minInput,
        networkFee = this.networkFee,
        rate = this.rate,
        rateMode = this.rateMode,
        state = this.state,
        stateError = this.stateError,
        svcFee = this.svcFee,
        toAmount = this.toAmount,
        toAddress = this.toAddress,
        toCurrency = this.toCurrency,
        transactionIdReceived = this.transactionIdReceived,
        transactionIdSent = this.transactionIdSent,
    )

fun OrderUpdate.toOrderUpdateWithArchiveEntity(archived: Boolean) =
    OrderUpdateWithArchive(
        id = this.id,
        archived = archived,
        createdAt = this.createdAt,
        fromAddr = this.fromAddr,
        fromCurrency = this.fromCurrency,
        fromAmountReceived = this.fromAmountReceived,
        maxInput = this.maxInput,
        minInput = this.minInput,
        networkFee = this.networkFee,
        rate = this.rate,
        rateMode = this.rateMode,
        state = this.state,
        stateError = this.stateError,
        svcFee = this.svcFee,
        toAmount = this.toAmount,
        toAddress = this.toAddress,
        toCurrency = this.toCurrency,
        transactionIdReceived = this.transactionIdReceived,
        transactionIdSent = this.transactionIdSent,
    )
