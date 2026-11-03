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

package com.movtery.zalithlauncher.ui.screens.main.control_editor.edit_widget

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.movtery.inputmap.keycodes.ControlEventKeyName
import com.movtery.layer_controller.data.ButtonShape
import com.movtery.layer_controller.data.ButtonSize
import com.movtery.layer_controller.data.ButtonStyle
import com.movtery.layer_controller.data.DefaultButtonStyle
import com.movtery.layer_controller.data.DefaultButtonStyleConfig
import com.movtery.layer_controller.data.MIN_SIZE_DP
import com.movtery.layer_controller.data.cloneNew
import com.movtery.layer_controller.event.ClickEvent
import com.movtery.layer_controller.observable.ObservableButtonStyle
import com.movtery.layer_controller.observable.ObservableControlLayout
import com.movtery.layer_controller.observable.ObservableNormalData
import com.movtery.layer_controller.observable.ObservableStyleConfig
import com.movtery.zalithlauncher.ui.control.Keyboard
import com.movtery.zalithlauncher.ui.screens.main.control_editor.InfoLayoutColorItem
import com.movtery.zalithlauncher.ui.screens.main.control_editor.InfoLayoutSliderItem
import com.movtery.zalithlauncher.ui.screens.main.control_editor.InfoLayoutSwitchItem
import kotlin.math.roundToInt

private val LabelColor = Color(0xFFBDBDBD)
private val LineColor = Color(0xFF8A8A8A)

private fun Float.fmt(): String = if (this % 1f == 0f) toInt().toString() else "%.1f".format(this)

/**
 * Setiap tombol punya gaya pribadi (agar mengubah warna/garis/sudut satu tombol
 * tidak mengubah tombol lain yang berbagi gaya yang sama), seperti di PojavLauncher.
 */
private fun ensurePersonalStyle(
    layout: ObservableControlLayout,
    data: ObservableNormalData
): ObservableButtonStyle {
    val key = "Pojav-${data.uuid.take(8)}"
    val current = layout.styles.value.firstOrNull { it.uuid == data.buttonStyle }
    if (current != null && current.name == key) return current

    val base: ButtonStyle = (current?.pack() ?: DefaultButtonStyle).cloneNew().copy(name = key)
    layout.addStyle(base)
    data.buttonStyle = base.uuid
    return layout.styles.value.first { it.uuid == base.uuid }
}

/**
 * Panel edit tombol ala PojavLauncher:
 * Name, Size, Mapping, Toggleable, Mouse pass-thru, Swipeable,
 * Background color, Stroke color, Stroke width, Corner radius.
 */
@Composable
fun PojavWidgetPanel(
    data: ObservableNormalData,
    layout: ObservableControlLayout,
    styles: List<ObservableButtonStyle>,
    onOpenAdvanced: () -> Unit,
    modifier: Modifier = Modifier
) {
    val config: ObservableStyleConfig? = styles.firstOrNull { it.uuid == data.buttonStyle }?.lightStyle
    val bgColor = config?.backgroundColor ?: DefaultButtonStyleConfig.backgroundColor
    val strokeColor = config?.borderColor ?: DefaultButtonStyleConfig.borderColor
    val strokeWidth = config?.borderWidth ?: DefaultButtonStyleConfig.borderWidth
    val radius = config?.borderRadius?.topStart ?: DefaultButtonStyleConfig.borderRadius.topStart

    fun editStyle(block: (ObservableStyleConfig) -> Unit) {
        val style = ensurePersonalStyle(layout, data)
        block(style.lightStyle)
        block(style.darkStyle)
    }

    var widthText by remember(data) { mutableStateOf(data.buttonSize.widthDp.fmt()) }
    var heightText by remember(data) { mutableStateOf(data.buttonSize.heightDp.fmt()) }
    var editingSlot by remember { mutableIntStateOf(-1) }

    val keyEvents = data.clickEvents.filter { it.type == ClickEvent.Type.Key }

    @Composable
    fun Slot(slot: Int, slotModifier: Modifier) {
        val key = keyEvents.getOrNull(slot)?.key
        val name = key?.let { ControlEventKeyName.getNameByKey(it) ?: it } ?: "UNKNOWN"
        Text(
            modifier = slotModifier
                .clickable { editingSlot = slot }
                .padding(vertical = 10.dp),
            text = name,
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.bodyLarge
        )
    }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        //Name
        item {
            Text(text = "Name", style = MaterialTheme.typography.labelLarge, color = LabelColor)
            PojavTextField(
                value = data.text.default,
                onValueChange = { data.text.default = it },
                keyboardType = KeyboardType.Text
            )
        }

        //Size
        item {
            Text(
                modifier = Modifier.padding(top = 8.dp),
                text = "Size",
                style = MaterialTheme.typography.labelLarge,
                color = LabelColor
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                PojavTextField(
                    modifier = Modifier.weight(1f),
                    value = widthText,
                    onValueChange = { text ->
                        widthText = text
                        text.toFloatOrNull()?.takeIf { it >= MIN_SIZE_DP }?.let { w ->
                            data.buttonSize = data.buttonSize.copy(type = ButtonSize.Type.Dp, widthDp = w)
                        }
                    },
                    keyboardType = KeyboardType.Decimal
                )
                Text(modifier = Modifier.padding(horizontal = 8.dp), text = "x", color = LabelColor)
                PojavTextField(
                    modifier = Modifier.weight(1f),
                    value = heightText,
                    onValueChange = { text ->
                        heightText = text
                        text.toFloatOrNull()?.takeIf { it >= MIN_SIZE_DP }?.let { h ->
                            data.buttonSize = data.buttonSize.copy(type = ButtonSize.Type.Dp, heightDp = h)
                        }
                    },
                    keyboardType = KeyboardType.Decimal
                )
            }
        }

        //Mapping
        item {
            Text(
                modifier = Modifier.padding(top = 8.dp),
                text = "Mapping",
                style = MaterialTheme.typography.labelLarge,
                color = LabelColor
            )
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Slot(0, Modifier.weight(1f))
                Text(text = "+", color = LabelColor)
                Slot(1, Modifier.weight(1f))
            }
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Slot(2, Modifier.weight(1f))
                Text(text = "+", color = LabelColor)
                Slot(3, Modifier.weight(1f))
            }
            Text(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { data.removeAllEvent(ClickEvent.Type.Key) }
                    .padding(vertical = 8.dp),
                text = "CLEAR MAPPING",
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.labelMedium,
                color = LabelColor
            )
        }

        //Toggleable / Mouse pass-thru / Swipeable
        item {
            InfoLayoutSwitchItem(
                modifier = Modifier.fillMaxWidth(),
                title = "Toggleable",
                value = data.isToggleable,
                onValueChange = { data.isToggleable = it },
                color = Color.Transparent
            )
        }
        item {
            InfoLayoutSwitchItem(
                modifier = Modifier.fillMaxWidth(),
                title = "Mouse pass-thru",
                value = data.isPenetrable,
                onValueChange = { data.isPenetrable = it },
                color = Color.Transparent
            )
        }
        item {
            InfoLayoutSwitchItem(
                modifier = Modifier.fillMaxWidth(),
                title = "Swipeable",
                value = data.isSwipple,
                onValueChange = { data.isSwipple = it },
                color = Color.Transparent
            )
        }

        //Background color / Stroke color
        item {
            InfoLayoutColorItem(
                modifier = Modifier.fillMaxWidth(),
                title = "Background color",
                color = bgColor,
                onColorChanged = { c -> editStyle { it.backgroundColor = c } }
            )
        }
        item {
            InfoLayoutColorItem(
                modifier = Modifier.fillMaxWidth(),
                title = "Stroke color",
                color = strokeColor,
                onColorChanged = { c ->
                    editStyle {
                        it.borderColor = c
                        it.pressedBorderColor = c
                    }
                }
            )
        }

        //Stroke width
        item {
            InfoLayoutSliderItem(
                modifier = Modifier.fillMaxWidth(),
                title = "Stroke width",
                value = strokeWidth.toFloat(),
                onValueChange = { v ->
                    val w = v.roundToInt().coerceIn(0, 50)
                    editStyle {
                        it.borderWidth = w
                        it.pressedBorderWidth = w
                    }
                },
                valueRange = 0f..50f,
                decimalFormat = "#0",
                suffix = "dp",
                color = Color.Transparent
            )
        }

        //Corner radius
        item {
            InfoLayoutSliderItem(
                modifier = Modifier.fillMaxWidth(),
                title = "Corner radius",
                value = radius,
                onValueChange = { v ->
                    val r = v.coerceIn(0f, 100f)
                    editStyle {
                        it.borderRadius = ButtonShape(r)
                        it.pressedBorderRadius = ButtonShape(r)
                    }
                },
                valueRange = 0f..100f,
                decimalFormat = "#0",
                suffix = "dp",
                color = Color.Transparent
            )
        }

        //Pengaturan lanjutan Zalith (event peluncur, ganti lapisan, kirim teks, dsb.)
        item {
            Box(modifier = Modifier.fillMaxWidth()) {
                Text(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(onClick = onOpenAdvanced)
                        .padding(vertical = 14.dp),
                    text = "ADVANCED (EVENTS, LAYERS, STYLES)",
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.labelMedium,
                    color = LabelColor
                )
            }
        }
    }

    if (editingSlot >= 0) {
        Keyboard(
            onDismissRequest = { editingSlot = -1 },
            isTapMode = true,
            onTap = { selectedKey ->
                val keys = keyEvents.map { it.key }.toMutableList()
                if (editingSlot in keys.indices) keys[editingSlot] = selectedKey else keys.add(selectedKey)
                data.removeAllEvent(ClickEvent.Type.Key)
                keys.distinct().forEach { k ->
                    data.addEvent(ClickEvent(type = ClickEvent.Type.Key, key = k))
                }
                editingSlot = -1
            }
        )
    }
}

@Composable
private fun PojavTextField(
    value: String,
    onValueChange: (String) -> Unit,
    keyboardType: KeyboardType,
    modifier: Modifier = Modifier
) {
    androidx.compose.foundation.layout.Column(modifier = modifier) {
        BasicTextField(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            value = value,
            onValueChange = onValueChange,
            singleLine = true,
            textStyle = MaterialTheme.typography.bodyLarge.copy(
                color = Color.White,
                textAlign = TextAlign.Center
            ),
            cursorBrush = SolidColor(Color.White),
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType)
        )
        HorizontalDivider(color = LineColor)
    }
}
