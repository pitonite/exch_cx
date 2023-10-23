package io.github.pitonite.exch_cx.ui.components

import android.annotation.SuppressLint
import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import io.github.pitonite.exch_cx.R

@Composable
fun ExchDrawable(
    name: String,
    contentScale: ContentScale = ContentScale.Fit,
    contentDescription: String = name,
    colorFilter: ColorFilter? = null,
    @SuppressLint("ModifierParameter") modifier: Modifier = Modifier,
) {
  val context = LocalContext.current
  val drawableId =
      remember(name) {
        val id = context.resources.getIdentifier(name, "drawable", context.packageName)
        if (id == 0) R.drawable.generic else id
      }

  Image(
      painterResource(drawableId),
      contentDescription = contentDescription,
      contentScale = contentScale,
      modifier = modifier,
      colorFilter = colorFilter,
  )
}
