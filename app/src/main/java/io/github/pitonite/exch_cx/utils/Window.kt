package io.github.pitonite.exch_cx.utils

import android.view.View
import android.view.Window

// Extension function to find the window from a view
fun View.findWindow(): Window? {
  var context = this.context
  while (context is android.content.ContextWrapper) {
    if (context is android.app.Activity) {
      return context.window
    }
    context = context.baseContext
  }
  return null
}
