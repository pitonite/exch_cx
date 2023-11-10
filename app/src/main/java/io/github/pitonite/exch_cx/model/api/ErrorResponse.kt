package io.github.pitonite.exch_cx.model.api

import kotlinx.serialization.Serializable
import javax.annotation.concurrent.Immutable

@Immutable @Serializable data class ErrorResponse(val error: String)
