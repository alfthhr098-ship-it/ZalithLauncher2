/*
 * Zalith Launcher 2
 * Copyright (C) 2025-2026 MovTery <movtery228@qq.com> and contributors
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

package com.movtery.zalithlauncher.ui.screens.main.control_editor

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

/** Warna latar editor ala PojavLauncher */
val PojavEditorBackground = Color(0xFF141414)

/** Warna abu-abu menu/tombol aksi ala PojavLauncher */
val PojavEditorGray = Color(0xFF4A4A4A)

/** Warna aksen (teal) ala PojavLauncher */
val PojavEditorAccent = Color(0xFF80CBC4)

private val PojavEditorColors = darkColorScheme(
    primary = PojavEditorAccent,
    onPrimary = Color(0xFF003732),
    primaryContainer = PojavEditorGray,
    onPrimaryContainer = Color.White,
    secondary = PojavEditorAccent,
    onSecondary = Color(0xFF003732),
    secondaryContainer = PojavEditorGray,
    onSecondaryContainer = Color.White,
    tertiary = PojavEditorAccent,
    onTertiary = Color(0xFF003732),
    tertiaryContainer = PojavEditorGray,
    onTertiaryContainer = Color.White,
    background = PojavEditorBackground,
    onBackground = Color.White,
    surface = Color(0xFF1C1C1C),
    onSurface = Color.White,
    surfaceVariant = Color(0xFF303030),
    onSurfaceVariant = Color(0xFFE0E0E0),
    outline = Color(0xFF8A8A8A),
    outlineVariant = Color(0xFF555555),
    surfaceDim = Color(0xFF141414),
    surfaceBright = Color(0xFF3A3A3A),
    surfaceContainerLowest = Color(0xFF161616),
    surfaceContainerLow = Color(0xFF1C1C1C),
    surfaceContainer = Color(0xFF242424),
    surfaceContainerHigh = Color(0xFF2E2E2E),
    surfaceContainerHighest = Color(0xFF383838),
)

private val PojavEditorShape = RoundedCornerShape(2.dp)

private val PojavEditorShapes = Shapes(
    extraSmall = PojavEditorShape,
    small = PojavEditorShape,
    medium = PojavEditorShape,
    large = PojavEditorShape,
    extraLarge = PojavEditorShape
)

/**
 * Tema editor kontrol: gelap, datar, sudut hampir kotak, aksen teal (gaya PojavLauncher)
 */
@Composable
fun PojavEditorTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = PojavEditorColors,
        typography = MaterialTheme.typography,
        shapes = PojavEditorShapes,
        content = content
    )
}
