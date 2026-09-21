package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme =
  darkColorScheme(
    primary = MeadowGreenLight,
    onPrimary = MeadowOnSurfaceLight,
    secondary = SoilBrownLight,
    tertiary = GoldenHoneyAccent,
    background = MeadowBackgroundDark,
    surface = MeadowSurfaceDark,
    onBackground = MeadowOnSurfaceDark,
    onSurface = MeadowOnSurfaceDark
  )

private val LightColorScheme =
  lightColorScheme(
    primary = MeadowGreenPrimary,
    onPrimary = androidx.compose.ui.graphics.Color.White,
    secondary = SoilBrownSecondary,
    onSecondary = androidx.compose.ui.graphics.Color.White,
    tertiary = GoldenHoneyAccent,
    background = MeadowBackgroundLight,
    surface = MeadowSurfaceLight,
    onBackground = MeadowOnSurfaceLight,
    onSurface = MeadowOnSurfaceLight
  )

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  // Dynamic color is available on Android 12+
  dynamicColor: Boolean = true,
  content: @Composable () -> Unit,
) {
  val colorScheme =
    when {
      dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
        val context = LocalContext.current
        if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
      }

      darkTheme -> DarkColorScheme
      else -> LightColorScheme
    }

  MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}
