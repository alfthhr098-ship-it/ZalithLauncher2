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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/gpl-3.0.txt>.
 */
package com.movtery.zalithlauncher.ui.screens.main.control_editor

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.movtery.zalithlauncher.R

/**
 * Pojav 风格的编辑器菜单：
 * 顶部居中的齿轮图标 + 点击后从右侧滑出的纯文字菜单。
 * 点击控件即可编辑（设置/复制/删除）由 [ControlEditor] 中已有的浮动按钮提供。
 */
@Composable
fun PojavEditorCog(
    modifier: Modifier = Modifier,
    isPreviewMode: Boolean,
    onAddButton: () -> Unit,
    onAddText: () -> Unit,
    onAddJoystick: () -> Unit,
    onStyles: () -> Unit,
    onAllOptions: () -> Unit,
    onTogglePreview: () -> Unit,
    onSave: () -> Unit,
    onSaveAndExit: () -> Unit,
    onExit: () -> Unit
) {
    var open by rememberSaveable { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize()) {
        if (!isPreviewMode && !open) {
            Text(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(start = 16.dp, end = 16.dp, top = 42.dp),
                text = "Tap the top center cog icon to open the context menu.\n" +
                        "Tap a button to customize: edit, resize, clone, delete.",
                color = Color.White.copy(alpha = 0.45f),
                fontSize = 11.sp,
                textAlign = TextAlign.Center
            )
        }

        //点击菜单外部关闭菜单
        if (open) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) { open = false }
            )
        }

        Surface(
            modifier = modifier
                .align(Alignment.TopCenter)
                .padding(top = 4.dp)
                .size(32.dp),
            shape = CircleShape,
            color = Color(0xCC303030),
            contentColor = Color.White,
            onClick = { open = !open }
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    modifier = Modifier.size(20.dp),
                    painter = painterResource(R.drawable.ic_settings_filled),
                    contentDescription = null
                )
            }
        }

        AnimatedVisibility(
            modifier = Modifier.align(Alignment.CenterEnd),
            visible = open,
            enter = slideInHorizontally { it },
            exit = slideOutHorizontally { it }
        ) {
            Column(
                modifier = Modifier
                    .width(240.dp)
                    .fillMaxHeight()
                    .background(Color(0xFF4A4A4A))
                    .verticalScroll(rememberScrollState())
            ) {
                val close = { open = false }
                PojavMenuItem("Add button") { close(); onAddButton() }
                PojavMenuItem("Add text") { close(); onAddText() }
                PojavMenuItem("Add joystick") { close(); onAddJoystick() }
                PojavMenuItem("Button styles") { close(); onStyles() }
                PojavMenuItem("Layers & more options") { close(); onAllOptions() }
                PojavMenuItem(if (isPreviewMode) "Exit preview" else "Preview") { close(); onTogglePreview() }
                PojavMenuItem("Save") { close(); onSave() }
                PojavMenuItem("Save and exit") { close(); onSaveAndExit() }
                PojavMenuItem("Exit") { close(); onExit() }
            }
        }
    }
}

@Composable
private fun PojavMenuItem(
    text: String,
    onClick: () -> Unit
) {
    Text(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 20.dp, vertical = 14.dp),
        text = text,
        color = Color.White,
        style = MaterialTheme.typography.bodyLarge
    )
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(1.dp)
            .background(Color(0x22FFFFFF))
    )
}
