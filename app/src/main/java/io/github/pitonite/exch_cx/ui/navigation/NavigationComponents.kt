package io.github.pitonite.exch_cx.ui.navigation

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuOpen
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.MenuOpen
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.PermanentDrawerSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.Measurable
import androidx.compose.ui.layout.MeasurePolicy
import androidx.compose.ui.layout.layoutId
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.offset
import io.github.pitonite.exch_cx.R
import io.github.pitonite.exch_cx.ui.theme.ExchTheme
import io.github.pitonite.exch_cx.utils.ExchNavigationContentPosition

// update from
// https://github.com/android/compose-samples/blob/main/Reply/app/src/main/java/com/example/reply/ui/navigation/ReplyNavigationComponents.kt
// when todo is removed there

@Composable
fun ExchNavigationRail(
    selectedDestination: String,
    navigationContentPosition: ExchNavigationContentPosition,
    navigateTo: (String) -> Unit,
    onDrawerClicked: () -> Unit = {},
) {

  NavigationRail(
      modifier = Modifier.fillMaxHeight(),
      containerColor = MaterialTheme.colorScheme.inverseOnSurface) {
        Column(
            modifier = Modifier.layoutId(LayoutType.HEADER),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)) {
              NavigationRailItem(
                  selected = false,
                  onClick = onDrawerClicked,
                  icon = {
                    Icon(
                        imageVector = Icons.Default.Menu,
                        contentDescription = stringResource(id = R.string.navigation_drawer))
                  })
              Spacer(Modifier.height(8.dp)) // NavigationRailHeaderPadding
              Spacer(Modifier.height(4.dp)) // NavigationRailVerticalPadding
        }

        Column(
            modifier = Modifier.layoutId(LayoutType.CONTENT),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)) {
              PrimaryDestinations.entries.forEach { section ->
                NavigationRailItem(
                    selected = selectedDestination.startsWith(section.route),
                    onClick = { navigateTo(section.route) },
                    icon = {
                      Icon(
                          imageVector = section.icon,
                          contentDescription = stringResource(id = section.title))
                    })
              }

              NavigationRailItem(
                  selected = selectedDestination == SecondaryDestinations.ALERTS_ROUTE,
                  onClick = { navigateTo(SecondaryDestinations.ALERTS_ROUTE) },
                  icon = {
                    Icon(
                        imageVector = Icons.Default.NotificationsActive,
                        contentDescription = stringResource(R.string.alerts))
                  },
              )

              NavigationRailItem(
                  selected = selectedDestination == SecondaryDestinations.SETTINGS_ROUTE,
                  onClick = { navigateTo(SecondaryDestinations.SETTINGS_ROUTE) },
                  icon = {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = stringResource(R.string.settings))
                  },
              )
            }
      }
}

@Composable
fun ExchBottomNavigationBar(
    modifier: Modifier = Modifier,
    selectedDestination: String,
    navigateTo: (String) -> Unit
) {
  NavigationBar(modifier = modifier.fillMaxWidth()) {
    PrimaryDestinations.entries.forEach { section ->
      NavigationBarItem(
          selected = selectedDestination.startsWith(section.route),
          onClick = { navigateTo(section.route) },
          icon = {
            Icon(
                imageVector = section.icon, contentDescription = stringResource(id = section.title))
          },
          label = { Text(stringResource(id = section.title)) },
      )
    }
  }
}

@Preview("default")
@Preview("large font", fontScale = 2f)
@Composable
fun ExchBottomNavigationBarPreview() {
  ExchTheme(darkTheme = true) {
    ExchBottomNavigationBar(
        selectedDestination = PrimaryDestinations.EXCHANGE.route, navigateTo = {})
  }
}

@Composable
fun PermanentNavigationDrawerContent(
    selectedDestination: String,
    navigationContentPosition: ExchNavigationContentPosition,
    navigateTo: (String) -> Unit,
) {
  PermanentDrawerSheet(
      modifier = Modifier.sizeIn(minWidth = 200.dp, maxWidth = 300.dp),
      drawerContainerColor = MaterialTheme.colorScheme.inverseOnSurface,
  ) {
    // TODO remove custom nav drawer content positioning when NavDrawer component supports it.
    // ticket : b/232495216
    Layout(
        modifier = Modifier.background(MaterialTheme.colorScheme.inverseOnSurface).padding(16.dp),
        content = {
          Column(
              modifier = Modifier.layoutId(LayoutType.HEADER),
              horizontalAlignment = Alignment.Start,
              verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    modifier = Modifier.padding(16.dp),
                    text = stringResource(id = R.string.app_name).uppercase(),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary)
              }

          Column(
              modifier =
                  Modifier.layoutId(LayoutType.CONTENT).verticalScroll(rememberScrollState()),
              horizontalAlignment = Alignment.CenterHorizontally,
          ) {
            PrimaryDestinations.entries.forEach { section ->
              NavigationDrawerItem(
                  selected = selectedDestination.startsWith(section.route),
                  label = {
                    Text(
                        text = stringResource(id = section.title),
                        modifier = Modifier.padding(horizontal = 16.dp))
                  },
                  icon = {
                    Icon(
                        imageVector = section.icon,
                        contentDescription = stringResource(id = section.title))
                  },
                  colors =
                      NavigationDrawerItemDefaults.colors(
                          unselectedContainerColor = Color.Transparent),
                  onClick = { navigateTo(section.route) })
            }

            NavigationDrawerItem(
                selected = selectedDestination == SecondaryDestinations.ALERTS_ROUTE,
                label = {
                  Text(
                      text = stringResource(R.string.alerts),
                      modifier = Modifier.padding(horizontal = 16.dp))
                },
                icon = {
                  Icon(
                      imageVector = Icons.Default.NotificationsActive,
                      contentDescription = stringResource(R.string.alerts))
                },
                colors =
                    NavigationDrawerItemDefaults.colors(
                        unselectedContainerColor = Color.Transparent),
                onClick = { navigateTo(SecondaryDestinations.ALERTS_ROUTE) },
            )

            NavigationDrawerItem(
                selected = selectedDestination == SecondaryDestinations.SETTINGS_ROUTE,
                label = {
                  Text(
                      text = stringResource(R.string.settings),
                      modifier = Modifier.padding(horizontal = 16.dp))
                },
                icon = {
                  Icon(
                      imageVector = Icons.Default.Settings,
                      contentDescription = stringResource(R.string.settings))
                },
                colors =
                    NavigationDrawerItemDefaults.colors(
                        unselectedContainerColor = Color.Transparent),
                onClick = { navigateTo(SecondaryDestinations.SETTINGS_ROUTE) },
            )
          }
        },
        measurePolicy = navigationMeasurePolicy(navigationContentPosition))
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ModalNavigationDrawerContent(
    selectedDestination: String,
    navigationContentPosition: ExchNavigationContentPosition,
    navigateTo: (String) -> Unit,
    onDrawerClicked: () -> Unit = {}
) {
  ModalDrawerSheet {
    // TODO remove custom nav drawer content positioning when NavDrawer component supports it.
    // ticket : b/232495216
    Layout(
        modifier = Modifier.background(MaterialTheme.colorScheme.inverseOnSurface).padding(16.dp),
        content = {
          Column(
              modifier = Modifier.layoutId(LayoutType.HEADER),
              horizontalAlignment = Alignment.CenterHorizontally,
              verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically) {
                      Text(
                          text = stringResource(id = R.string.app_name).uppercase(),
                          style = MaterialTheme.typography.titleMedium,
                          color = MaterialTheme.colorScheme.primary)
                      IconButton(onClick = onDrawerClicked) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.MenuOpen,
                            contentDescription = stringResource(id = R.string.navigation_drawer))
                      }
                    }
              }

          Column(
              modifier =
                  Modifier.layoutId(LayoutType.CONTENT).verticalScroll(rememberScrollState()),
              horizontalAlignment = Alignment.CenterHorizontally,
          ) {
            PrimaryDestinations.entries.forEach { section ->
              NavigationDrawerItem(
                  selected = selectedDestination.startsWith(section.route),
                  label = {
                    Text(
                        text = stringResource(id = section.title),
                        modifier = Modifier.padding(horizontal = 16.dp))
                  },
                  icon = {
                    Icon(
                        imageVector = section.icon,
                        contentDescription = stringResource(id = section.title))
                  },
                  colors =
                      NavigationDrawerItemDefaults.colors(
                          unselectedContainerColor = Color.Transparent),
                  onClick = { navigateTo(section.route) })
            }

            NavigationDrawerItem(
                selected = selectedDestination == SecondaryDestinations.ALERTS_ROUTE,
                label = {
                  Text(
                      text = stringResource(R.string.alerts),
                      modifier = Modifier.padding(horizontal = 16.dp))
                },
                icon = {
                  Icon(
                      imageVector = Icons.Default.NotificationsActive,
                      contentDescription = stringResource(R.string.alerts))
                },
                colors =
                    NavigationDrawerItemDefaults.colors(
                        unselectedContainerColor = Color.Transparent),
                onClick = { navigateTo(SecondaryDestinations.ALERTS_ROUTE) },
            )

            NavigationDrawerItem(
                selected = selectedDestination == SecondaryDestinations.SETTINGS_ROUTE,
                label = {
                  Text(
                      text = stringResource(R.string.settings),
                      modifier = Modifier.padding(horizontal = 16.dp))
                },
                icon = {
                  Icon(
                      imageVector = Icons.Default.Settings,
                      contentDescription = stringResource(R.string.settings))
                },
                colors =
                    NavigationDrawerItemDefaults.colors(
                        unselectedContainerColor = Color.Transparent),
                onClick = { navigateTo(SecondaryDestinations.SETTINGS_ROUTE) },
            )
          }
        },
        measurePolicy = navigationMeasurePolicy(navigationContentPosition))
  }
}

fun navigationMeasurePolicy(
    navigationContentPosition: ExchNavigationContentPosition,
): MeasurePolicy {
  return MeasurePolicy { measurables, constraints ->
    lateinit var headerMeasurable: Measurable
    lateinit var contentMeasurable: Measurable
    measurables.forEach {
      when (it.layoutId) {
        LayoutType.HEADER -> headerMeasurable = it
        LayoutType.CONTENT -> contentMeasurable = it
        else -> error("Unknown layoutId encountered!")
      }
    }

    val headerPlaceable = headerMeasurable.measure(constraints)
    val contentPlaceable =
        contentMeasurable.measure(constraints.offset(vertical = -headerPlaceable.height))
    layout(constraints.maxWidth, constraints.maxHeight) {
      // Place the header, this goes at the top
      headerPlaceable.placeRelative(0, 0)

      // Determine how much space is not taken up by the content
      val nonContentVerticalSpace = constraints.maxHeight - contentPlaceable.height

      val contentPlaceableY =
          when (navigationContentPosition) {
            // Figure out the place we want to place the content, with respect to the
            // parent (ignoring the header for now)
            ExchNavigationContentPosition.TOP -> 0
            ExchNavigationContentPosition.CENTER -> nonContentVerticalSpace / 2
          }
          // And finally, make sure we don't overlap with the header.
          .coerceAtLeast(headerPlaceable.height)

      contentPlaceable.placeRelative(0, contentPlaceableY)
    }
  }
}

enum class LayoutType {
  HEADER,
  CONTENT
}

enum class ScaleTransitionDirection() {
  INWARDS,
  OUTWARDS
}

fun scaleIntoContainer(
    direction: ScaleTransitionDirection = ScaleTransitionDirection.INWARDS,
    initialScale: Float = if (direction == ScaleTransitionDirection.OUTWARDS) 0.9f else 1.1f
): EnterTransition {
  return scaleIn(animationSpec = tween(220, delayMillis = 90), initialScale = initialScale) +
      fadeIn(animationSpec = tween(220, delayMillis = 90))
}

fun scaleOutOfContainer(
    direction: ScaleTransitionDirection = ScaleTransitionDirection.OUTWARDS,
    targetScale: Float = if (direction == ScaleTransitionDirection.INWARDS) 0.9f else 1.1f
): ExitTransition {
  return scaleOut(
      animationSpec = tween(durationMillis = 220, delayMillis = 90), targetScale = targetScale) +
      fadeOut(tween(delayMillis = 90))
}
