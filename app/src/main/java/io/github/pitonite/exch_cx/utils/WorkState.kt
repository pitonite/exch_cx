package io.github.pitonite.exch_cx.utils

import javax.annotation.concurrent.Immutable

@Immutable
sealed class WorkState() {
  /** Indicates the work is currently done, and no error was observed. */
  data object NotWorking : WorkState()

  /** work is in progress. */
  data object Working : WorkState()

  /**
   * Work hit an error.
   *
   * @param error [Throwable] that caused the work operation to generate this error state.
   */
  @Immutable
  class Error(public val error: Throwable) : WorkState() {
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
}

@Immutable
sealed class ExchangeWorkState() : WorkState() {
  data object Refreshing : ExchangeWorkState()
}
