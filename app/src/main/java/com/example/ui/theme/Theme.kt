package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme =
  darkColorScheme(
    primary = Color(0xFF90CAF9),
    onPrimary = Color(0xFF0D1B2A),
    primaryContainer = ScholarNavy,
    onPrimaryContainer = Color(0xFFE2E8F0),
    secondary = ScienceTeal,
    onSecondary = Color.White,
    tertiary = AcademicGoldLight,
    onTertiary = Color.Black,
    background = MidnightDark,
    onBackground = DarkTextPrimary,
    surface = MidnightSurface,
    onSurface = DarkTextPrimary,
    surfaceVariant = MidnightCard,
    onSurfaceVariant = DarkTextSecondary,
    outline = MidnightBorder
  )

private val LightColorScheme =
  lightColorScheme(
    primary = ScholarNavy,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFE0E7FF),
    onPrimaryContainer = ScholarNavyDark,
    secondary = ScienceTeal,
    onSecondary = Color.White,
    tertiary = AcademicGold,
    onTertiary = Color.White,
    background = BackgroundSlate,
    onBackground = TextPrimaryNavy,
    surface = SurfaceWhite,
    onSurface = TextPrimaryNavy,
    surfaceVariant = SurfaceVariantSlate,
    onSurfaceVariant = TextSecondarySlate,
    outline = CardBorderSlate
  )

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  dynamicColor: Boolean = false, // Keep consistent high-polish academic branding
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

