/*
 * Zalith Launcher 2
 * Copyright (C) 2025-2026 MovTery <movtery228@qq.com> and contributors
 *
 * Modified for Horus Launcher: pure black (AMOLED) palette.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/gpl-3.0.txt>.
 */

package com.movtery.zalithlauncher.ui.theme

import android.os.Build
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialExpressiveTheme
import androidx.compose.material3.MotionScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import com.materialkolor.PaletteStyle
import com.materialkolor.dynamicColorScheme
import com.movtery.zalithlauncher.setting.AllSettings
import com.movtery.zalithlauncher.setting.enums.isLauncherInDarkTheme
import com.movtery.zalithlauncher.ui.theme.components.activeMaskView
import com.movtery.zalithlauncher.utils.festival.Festival
import com.movtery.zalithlauncher.utils.festival.LocalFestivals
import com.movtery.zalithlauncher.viewmodel.BackgroundViewModel
import com.movtery.zalithlauncher.viewmodel.LocalBackgroundViewModel

/** Horus Launcher black palette (dark). */
private val horusDark = darkColorScheme(
    primary = Color(0xFFFFFFFF),
    onPrimary = Color(0xFF000000),
    primaryContainer = Color(0xFF2A2A2A),
    onPrimaryContainer = Color(0xFFFFFFFF),
    secondary = Color(0xFFBDBDBD),
    onSecondary = Color(0xFF000000),
    secondaryContainer = Color(0xFF242424),
    onSecondaryContainer = Color(0xFFE0E0E0),
    tertiary = Color(0xFF9E9E9E),
    onTertiary = Color(0xFF000000),
    tertiaryContainer = Color(0xFF1F1F1F),
    onTertiaryContainer = Color(0xFFE0E0E0),
    error = Color(0xFFFF5252),
    onError = Color(0xFF000000),
    errorContainer = Color(0xFF3A0000),
    onErrorContainer = Color(0xFFFFDAD6),
    background = Color(0xFF000000),
    onBackground = Color(0xFFFFFFFF),
    surface = Color(0xFF000000),
    onSurface = Color(0xFFFFFFFF),
    surfaceVariant = Color(0xFF1A1A1A),
    onSurfaceVariant = Color(0xFFBDBDBD),
    outline = Color(0xFF3A3A3A),
    outlineVariant = Color(0xFF262626),
    scrim = Color(0xFF000000),
    inverseSurface = Color(0xFFEDEDED),
    inverseOnSurface = Color(0xFF111111),
    inversePrimary = Color(0xFF111111),
)

/** Horus Launcher light palette (monochrome). */
private val horusLight = lightColorScheme(
    primary = Color(0xFF111111),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFE6E6E6),
    onPrimaryContainer = Color(0xFF111111),
    secondary = Color(0xFF444444),
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFECECEC),
    onSecondaryContainer = Color(0xFF1A1A1A),
    background = Color(0xFFFFFFFF),
    onBackground = Color(0xFF111111),
    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF111111),
    surfaceVariant = Color(0xFFEFEFEF),
    onSurfaceVariant = Color(0xFF444444),
    outline = Color(0xFF8A8A8A),
)

/** Force every dark scheme (dynamic, custom or preset) onto true-black surfaces for AMOLED screens. */
private fun ColorScheme.toAmoled(): ColorScheme = copy(
    background = Color(0xFF000000),
    surface = Color(0xFF000000),
    surfaceDim = Color(0xFF000000),
    surfaceContainerLowest = Color(0xFF000000),
    surfaceContainerLow = Color(0xFF0A0A0A),
    surfaceContainer = Color(0xFF111111),
    surfaceContainerHigh = Color(0xFF171717),
    surfaceContainerHighest = Color(0xFF1E1E1E),
    surfaceBright = Color(0xFF242424),
    surfaceVariant = Color(0xFF1A1A1A),
)

private fun customLight(
    color: Color,
    style: PaletteStyle,
): ColorScheme {
    return dynamicColorScheme(
        primary = color,
        isDark = false,
        style = style,
    )
}

private fun customDark(
    color: Color,
    style: PaletteStyle,
): ColorScheme {
    return dynamicColorScheme(
        primary = color,
        isDark = true,
        style = style,
    )
}

@Composable
fun ZalithLauncherTheme(
    darkTheme: Boolean = isLauncherInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = true,
    backgroundViewModel: BackgroundViewModel? = null,
    festivals: List<Festival> = emptyList(),
    content: @Composable () -> Unit
) {
    val colorTheme = AllSettings.launcherColorTheme.state
    val customColorInt = AllSettings.launcherCustomColor.state
    val customColor = Color(customColorInt)
    val customPaletteStyle = AllSettings.launcherCustomPaletteStyle.state

    val context = LocalContext.current

    val targetColorScheme = remember(darkTheme, dynamicColor, colorTheme, customColor, customPaletteStyle) {
        when {
            dynamicColor && colorTheme == ColorThemeType.DYNAMIC && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
                if (darkTheme) dynamicDarkColorScheme(context).toAmoled() else dynamicLightColorScheme(context)
            }

            colorTheme == ColorThemeType.CUSTOM -> {
                if (darkTheme) customDark(customColor, customPaletteStyle).toAmoled()
                else customLight(customColor, customPaletteStyle)
            }

            else -> if (darkTheme) horusDark else horusLight
        }
    }

    var currentDarkTheme by remember { mutableStateOf(darkTheme) }
    var currentDisplayScheme by remember { mutableStateOf(targetColorScheme) }

    LaunchedEffect(darkTheme, targetColorScheme) {
        if (darkTheme != currentDarkTheme) {
            context.activeMaskView(
                maskComplete = {
                    currentDarkTheme = darkTheme
                    currentDisplayScheme = targetColorScheme
                },
                maskAnimFinish = {

                }
            )
        } else {
            currentDarkTheme = darkTheme
            currentDisplayScheme = targetColorScheme
        }
    }

    CompositionLocalProvider(
        LocalBackgroundViewModel provides backgroundViewModel,
        LocalFestivals provides festivals
    ) {
        MaterialExpressiveTheme(
            colorScheme = currentDisplayScheme,
            motionScheme = MotionScheme.expressive(),
            typography = AppTypography,
            content = content
        )
    }
}
