package io.github.pitonite.exch_cx.utils

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.debugInspectorInfo
import androidx.compose.ui.unit.Dp

fun Modifier.noRippleClickable(onClick: () -> Unit): Modifier = composed {
  clickable(indication = null, interactionSource = remember { MutableInteractionSource() }) {
    onClick()
  }
}

fun Modifier.horizontalFadingEdge(
    scrollState: ScrollState,
    length: Dp,
    edgeColor: Color? = null,
) =
    composed(
        debugInspectorInfo {
          name = "length"
          value = length
        }) {
          val color = edgeColor ?: MaterialTheme.colorScheme.surface

          drawWithContent {
            val lengthValue = length.toPx()
            val scrollFromStart by derivedStateOf { scrollState.value }
            val scrollFromEnd by derivedStateOf { scrollState.maxValue - scrollState.value }

            val startFadingEdgeStrength =
                lengthValue * (scrollFromStart / lengthValue).coerceAtMost(1f)

            val endFadingEdgeStrength = lengthValue * (scrollFromEnd / lengthValue).coerceAtMost(1f)

            drawContent()

            drawRect(
                brush =
                    Brush.horizontalGradient(
                        colors =
                            listOf(
                                color,
                                Color.Transparent,
                            ),
                        startX = 0f,
                        endX = startFadingEdgeStrength,
                    ),
                size =
                    Size(
                        startFadingEdgeStrength,
                        this.size.height,
                    ),
            )

            drawRect(
                brush =
                    Brush.horizontalGradient(
                        colors =
                            listOf(
                                Color.Transparent,
                                color,
                            ),
                        startX = size.width - endFadingEdgeStrength,
                        endX = size.width,
                    ),
                topLeft = Offset(x = size.width - endFadingEdgeStrength, y = 0f),
            )
          }
        }

fun Modifier.verticalFadingEdge(
    scrollState: ScrollState,
    length: Dp,
    edgeColor: Color? = null,
) =
    composed(
        debugInspectorInfo {
          name = "length"
          value = length
        }) {
          val color = edgeColor ?: MaterialTheme.colorScheme.surface

          drawWithContent {
            val lengthValue = length.toPx()
            val scrollFromTop by derivedStateOf { scrollState.value }
            val scrollFromBottom by derivedStateOf { scrollState.maxValue - scrollState.value }

            val topFadingEdgeStrength = lengthValue * (scrollFromTop / lengthValue).coerceAtMost(1f)

            val bottomFadingEdgeStrength =
                lengthValue * (scrollFromBottom / lengthValue).coerceAtMost(1f)

            drawContent()

            drawRect(
                brush =
                    Brush.verticalGradient(
                        colors =
                            listOf(
                                color,
                                Color.Transparent,
                            ),
                        startY = 0f,
                        endY = topFadingEdgeStrength,
                    ),
                size = size.copy(height = topFadingEdgeStrength),
            )

            drawRect(
                brush =
                    Brush.verticalGradient(
                        colors =
                            listOf(
                                Color.Transparent,
                                color,
                            ),
                        startY = size.height - bottomFadingEdgeStrength,
                        endY = size.height,
                    ),
                topLeft = Offset(x = 0f, y = size.height - bottomFadingEdgeStrength),
            )
          }
        }

fun Modifier.verticalFadingEdge(
    lazyListState: LazyListState,
    length: Dp,
    edgeColor: Color? = null,
) =
    composed(
        debugInspectorInfo {
          name = "length"
          value = length
        }) {
          val color = edgeColor ?: MaterialTheme.colorScheme.surface

          drawWithContent {
            val topFadingEdgeStrength by derivedStateOf {
              lazyListState.layoutInfo
                  .run {
                    val firstItem = visibleItemsInfo.first()
                    when {
                      visibleItemsInfo.size in 0..1 -> 0f
                      firstItem.index > 0 -> 1f // Added
                      firstItem.offset == viewportStartOffset -> 0f
                      firstItem.offset < viewportStartOffset ->
                          firstItem.run { kotlin.math.abs(offset) / size.toFloat() }
                      else -> 1f
                    }
                  }
                  .coerceAtMost(1f) * length.value
            }
            val bottomFadingEdgeStrength by derivedStateOf {
              lazyListState.layoutInfo
                  .run {
                    val lastItem = visibleItemsInfo.last()
                    when {
                      visibleItemsInfo.size in 0..1 -> 0f
                      lastItem.index < totalItemsCount - 1 -> 1f // Added
                      lastItem.offset + lastItem.size <= viewportEndOffset -> 0f // added the <=
                      lastItem.offset + lastItem.size > viewportEndOffset ->
                          lastItem.run {
                            (size - (viewportEndOffset - offset)) /
                                size.toFloat() // Fixed the percentage computation
                          }
                      else -> 1f
                    }
                  }
                  .coerceAtMost(1f) * length.value
            }

            drawContent()

            drawRect(
                brush =
                    Brush.verticalGradient(
                        colors =
                            listOf(
                                color,
                                Color.Transparent,
                            ),
                        startY = 0f,
                        endY = topFadingEdgeStrength,
                    ),
                size = size.copy(height = topFadingEdgeStrength),
            )

            drawRect(
                brush =
                    Brush.verticalGradient(
                        colors =
                            listOf(
                                Color.Transparent,
                                color,
                            ),
                        startY = size.height - bottomFadingEdgeStrength,
                        endY = size.height,
                    ),
                topLeft = Offset(x = 0f, y = size.height - bottomFadingEdgeStrength),
            )
          }
        }
