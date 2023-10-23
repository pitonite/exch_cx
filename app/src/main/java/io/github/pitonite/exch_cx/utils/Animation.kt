package io.github.pitonite.exch_cx.utils

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State

/**
 * usage: val angle by rememberRotateInfinite() ... Modifier .graphicsLayer { rotationZ = angle }
 */
@Composable
fun rememberRotateInfinite(): State<Float> {
  val infiniteTransition = rememberInfiniteTransition(label = "infinite rotate")
  return infiniteTransition.animateFloat(
      initialValue = 0F,
      targetValue = 360F,
      animationSpec = infiniteRepeatable(animation = tween(1000, easing = LinearEasing)),
      label = "infinite rotate animation")
}
