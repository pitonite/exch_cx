package io.github.pitonite.exch_cx.utils

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp

// from https://proandroiddev.com/preventing-font-scaling-in-jetpack-compose-8a2cd0f09d23

val TextUnit.nonScaledSp
  @Composable get() = (this.value / LocalDensity.current.fontScale).sp
