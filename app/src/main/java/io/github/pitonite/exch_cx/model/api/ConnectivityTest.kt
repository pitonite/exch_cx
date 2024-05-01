package io.github.pitonite.exch_cx.model.api

import androidx.compose.runtime.Immutable
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

enum class ConnectivityResult {
  OK
}

@Serializable
@Immutable
data class ConnectivityTest(
  @SerialName("result") val result: ConnectivityResult
)
