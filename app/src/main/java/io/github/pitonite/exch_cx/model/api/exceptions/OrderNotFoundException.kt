package io.github.pitonite.exch_cx.model.api.exceptions

import io.github.pitonite.exch_cx.R
import io.github.pitonite.exch_cx.exceptions.LocalizedException

class OrderNotFoundException() : LocalizedException(R.string.exception_order_not_found)
