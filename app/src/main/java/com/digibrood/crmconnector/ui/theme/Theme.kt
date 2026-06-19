package com.digibrood.crmconnector.ui.theme

import android.app.Activity
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

private val LightColors = lightColorScheme(
    primary = BrandBlue,
    onPrimary = Color.White,
    primaryContainer = BrandBlueDark,
    onPrimaryContainer = Color.White,
    secondary = BrandCyan,
    background = LightBackground,
    surface = LightSurface
)

private val DarkColors = darkColorScheme(
    primary = BrandCyan,
    onPrimary = Color.Black,
    primaryContainer = BrandBlueDark,
    onPrimaryContainer = Color.White,
    secondary = BrandCyan,
    background = DarkBackground,
    surface = DarkSurface
)

/** App theme. Uses dynamic color on Android 12+ when available. */
@Composable
fun CrmConnectorTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColors
        else -> LightColors
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = AppTypography,
        content = content
    )
}
