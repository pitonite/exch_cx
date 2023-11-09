package io.github.pitonite.exch_cx.utils

import javax.annotation.concurrent.Immutable

interface WorkingState

interface ErrorState

@Immutable
sealed class WorkState {

  /** Indicates the work is currently done, and no error was observed. */
  data object NotWorking : WorkState()

  /** work is in progress. two working state are equal if their key are equal. */
  data object Working : WorkState(), WorkingState

  /**
   * Work hit an error.
   *
   * @param error [Throwable] that caused the work operation to generate this error state.
   */
  @Immutable
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

@Immutable
sealed class ExchangeWorkState() : WorkState() {

  data object Refreshing : ExchangeWorkState(), WorkingState

  data object CreatingOrder : ExchangeWorkState(), WorkingState

  data object ToAddressRequiredError : ExchangeWorkState(), ErrorState
}
