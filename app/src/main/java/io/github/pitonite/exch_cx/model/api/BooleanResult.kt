package io.github.pitonite.exch_cx.model.api

import androidx.compose.runtime.Stable
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@Stable
data class BooleanResult(
  @SerialName("result")
  val result: Boolean,
)
