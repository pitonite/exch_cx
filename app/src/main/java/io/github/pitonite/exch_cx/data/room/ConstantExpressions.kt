package io.github.pitonite.exch_cx.data.room

const val CURRENT_TIMESTAMP_EXPRESSION =
    "(cast((julianday('now') - 2440587.5) * 86400 * 1000 as integer))"
