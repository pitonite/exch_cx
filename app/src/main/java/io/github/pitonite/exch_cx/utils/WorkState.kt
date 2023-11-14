package io.github.pitonite.exch_cx.utils

import androidx.compose.runtime.Stable

@Stable interface WorkingState

@Stable interface ErrorState

@Stable
sealed class WorkState {

  @Stable
  /** Indicates the work is currently done, and no error was observed. */
  data object NotWorking : WorkState()

  @Stable
  /** work is in progress. two working state are equal if their class are equal. */
  data class Working(val currentWorkProgress: Int = 0, val totalWorkItems: Int = 0) :
      WorkState(), WorkingState

  /**
   * Work hit an error.
   *
   * @param error [Throwable] that caused the work operation to generate this error state.
   */
  @Stable
  data class Error(val error: Throwable) : WorkState(), ErrorState {
    override fun equals(other: Any?): Boolean {
      return other is Error && error == other.error
    }

    override fun hashCode(): Int {
      return error.hashCode()
    }

    override fun toString(): String {
      return error.toString()
    }
  }

  companion object {
    fun isWorking(state: WorkState): Boolean = state is WorkingState

    fun isError(state: WorkState): Boolean = state is ErrorState
  }
}

@Stable
sealed class ExchangeWorkState() : WorkState() {

  @Stable data object Refreshing : ExchangeWorkState(), WorkingState

  @Stable data object CreatingOrder : ExchangeWorkState(), WorkingState

  @Stable data object ToAddressRequiredError : ExchangeWorkState(), ErrorState
}
