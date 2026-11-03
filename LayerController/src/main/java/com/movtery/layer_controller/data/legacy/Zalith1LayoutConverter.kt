/*
 * Horus Launcher
 * Converter for ZalithLauncher 1 / PojavLauncher control layouts.
 *
 * Based on Zalith Launcher 2, Copyright (C) 2025-2026 MovTery <movtery228@qq.com> and contributors
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

package com.movtery.layer_controller.data.legacy

import android.content.res.Resources
import androidx.compose.ui.graphics.Color
import com.movtery.layer_controller.EDITOR_VERSION
import com.movtery.layer_controller.data.ButtonPosition
import com.movtery.layer_controller.data.ButtonShape
import com.movtery.layer_controller.data.ButtonSize
import com.movtery.layer_controller.data.ButtonStyle
import com.movtery.layer_controller.data.DefaultSize
import com.movtery.layer_controller.data.NormalData
import com.movtery.layer_controller.data.TextAlignment
import com.movtery.layer_controller.data.VisibilityType
import com.movtery.layer_controller.data.lang.TranslatableString
import com.movtery.layer_controller.event.ClickEvent
import com.movtery.layer_controller.layout.ControlLayer
import com.movtery.layer_controller.layout.ControlLayout
import com.movtery.layer_controller.utils.randomUUID
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.doubleOrNull
import kotlinx.serialization.json.longOrNull
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

/**
 * Screen used to evaluate the position expressions of Zalith 1 layouts.
 */
data class Zalith1ReferenceScreen(
    val widthPx: Float,
    val heightPx: Float,
    val density: Float
) {
    companion object {
        /** Landscape size of the current device, with a safe fallback. */
        fun fromSystem(): Zalith1ReferenceScreen {
            val fromDevice = try {
                val dm = Resources.getSystem().displayMetrics
                val w = max(dm.widthPixels, dm.heightPixels).toFloat()
                val h = min(dm.widthPixels, dm.heightPixels).toFloat()
                if (w > 0f && h > 0f && dm.density > 0f) Zalith1ReferenceScreen(w, h, dm.density) else null
            } catch (_: Throwable) {
                null
            }
            return fromDevice ?: Zalith1ReferenceScreen(2400f, 1080f, 2.625f)
        }
    }
}

/** True when the JSON looks like a Zalith 1 / PojavLauncher control layout. */
internal fun isZalith1Layout(json: JsonObject): Boolean =
    json["editorVersion"] == null &&
            (json.containsKey("mControlDataList") || json.containsKey("mDrawerDataList"))

/**
 * Convert a Zalith 1 layout into a Zalith 2 [ControlLayout].
 *
 * Supported: normal buttons, drawers (as hidden layers), mouse/scroll/menu special buttons,
 * the GUI toggle button, colors, stroke and corner radius.
 * Not supported yet: joysticks and the virtual mouse toggle.
 */
fun convertZalith1Layout(
    json: JsonObject,
    screen: Zalith1ReferenceScreen = Zalith1ReferenceScreen.fromSystem()
): ControlLayout = Zalith1Converter(screen).convert(json)

private const val EVENT_SWITCH_IME = "launcher.event.switch_ime"
private const val EVENT_SWITCH_MENU = "launcher.event.switch_menu"
private const val EVENT_SCROLL_UP = "launcher.event.scroll_up"
private const val EVENT_SCROLL_DOWN = "launcher.event.scroll_down"

private data class LegacyButton(
    val name: String,
    val dynamicX: String,
    val dynamicY: String,
    val widthDp: Float,
    val heightDp: Float,
    val keycodes: List<Int>,
    val opacity: Float,
    val bgColor: Int,
    val strokeColor: Int,
    val strokeWidth: Float,
    val cornerRadius: Float,
    val isToggle: Boolean,
    val passThru: Boolean,
    val isSwipeable: Boolean,
    val displayInGame: Boolean,
    val displayInMenu: Boolean
)

private data class StyleKey(
    val bg: Int,
    val stroke: Int,
    val strokeWidth: Int,
    val radius: Float,
    val alpha: Float
)

private fun JsonObject.optStr(key: String): String? =
    (this[key] as? JsonPrimitive)?.contentOrNull

private fun JsonObject.optFloat(key: String, default: Float): Float =
    (this[key] as? JsonPrimitive)?.doubleOrNull?.toFloat() ?: default

private fun JsonObject.optInt(key: String, default: Int): Int =
    (this[key] as? JsonPrimitive)?.let { p ->
        p.longOrNull?.toInt() ?: p.doubleOrNull?.toInt()
    } ?: default

private fun JsonObject.optBool(key: String, default: Boolean): Boolean =
    (this[key] as? JsonPrimitive)?.booleanOrNull ?: default

private fun JsonObject.toLegacyButton(): LegacyButton {
    val codes = (this["keycodes"] as? JsonArray)
        ?.mapNotNull { el ->
            (el as? JsonPrimitive)?.let { p -> p.longOrNull?.toInt() ?: p.doubleOrNull?.toInt() }
        }
        ?: emptyList()
    return LegacyButton(
        name = optStr("name") ?: "",
        dynamicX = optStr("dynamicX") ?: "0",
        dynamicY = optStr("dynamicY") ?: "0",
        widthDp = optFloat("width", 50f),
        heightDp = optFloat("height", 50f),
        keycodes = codes,
        opacity = optFloat("opacity", 1f),
        bgColor = optInt("bgColor", 0x4D000000),
        strokeColor = optInt("strokeColor", -1),
        strokeWidth = optFloat("strokeWidth", 0f),
        cornerRadius = optFloat("cornerRadius", 0f),
        isToggle = optBool("isToggle", false),
        passThru = optBool("passThruEnabled", false),
        isSwipeable = optBool("isSwipeable", false),
        displayInGame = optBool("displayInGame", true),
        displayInMenu = optBool("displayInMenu", true)
    )
}

/** Standard GLFW key codes to the key names used by Zalith 2 click events. */
private fun glfwKeyName(code: Int): String? = when (code) {
    32 -> "GLFW_KEY_SPACE"
    39 -> "GLFW_KEY_APOSTROPHE"
    44 -> "GLFW_KEY_COMMA"
    45 -> "GLFW_KEY_MINUS"
    46 -> "GLFW_KEY_PERIOD"
    47 -> "GLFW_KEY_SLASH"
    in 48..57 -> "GLFW_KEY_${code - 48}"
    59 -> "GLFW_KEY_SEMICOLON"
    61 -> "GLFW_KEY_EQUAL"
    in 65..90 -> "GLFW_KEY_${code.toChar()}"
    91 -> "GLFW_KEY_LEFT_BRACKET"
    92 -> "GLFW_KEY_BACKSLASH"
    93 -> "GLFW_KEY_RIGHT_BRACKET"
    96 -> "GLFW_KEY_GRAVE_ACCENT"
    161 -> "GLFW_KEY_WORLD_1"
    162 -> "GLFW_KEY_WORLD_2"
    256 -> "GLFW_KEY_ESCAPE"
    257 -> "GLFW_KEY_ENTER"
    258 -> "GLFW_KEY_TAB"
    259 -> "GLFW_KEY_BACKSPACE"
    260 -> "GLFW_KEY_INSERT"
    261 -> "GLFW_KEY_DELETE"
    262 -> "GLFW_KEY_RIGHT"
    263 -> "GLFW_KEY_LEFT"
    264 -> "GLFW_KEY_DOWN"
    265 -> "GLFW_KEY_UP"
    266 -> "GLFW_KEY_PAGE_UP"
    267 -> "GLFW_KEY_PAGE_DOWN"
    268 -> "GLFW_KEY_HOME"
    269 -> "GLFW_KEY_END"
    280 -> "GLFW_KEY_CAPS_LOCK"
    281 -> "GLFW_KEY_SCROLL_LOCK"
    282 -> "GLFW_KEY_NUM_LOCK"
    283 -> "GLFW_KEY_PRINT_SCREEN"
    284 -> "GLFW_KEY_PAUSE"
    in 290..314 -> "GLFW_KEY_F${code - 289}"
    in 320..329 -> "GLFW_KEY_KP_${code - 320}"
    330 -> "GLFW_KEY_KP_DECIMAL"
    331 -> "GLFW_KEY_KP_DIVIDE"
    332 -> "GLFW_KEY_KP_MULTIPLY"
    333 -> "GLFW_KEY_KP_SUBTRACT"
    334 -> "GLFW_KEY_KP_ADD"
    335 -> "GLFW_KEY_KP_ENTER"
    336 -> "GLFW_KEY_KP_EQUAL"
    340 -> "GLFW_KEY_LEFT_SHIFT"
    341 -> "GLFW_KEY_LEFT_CONTROL"
    342 -> "GLFW_KEY_LEFT_ALT"
    343 -> "GLFW_KEY_LEFT_SUPER"
    344 -> "GLFW_KEY_RIGHT_SHIFT"
    345 -> "GLFW_KEY_RIGHT_CONTROL"
    346 -> "GLFW_KEY_RIGHT_ALT"
    347 -> "GLFW_KEY_RIGHT_SUPER"
    348 -> "GLFW_KEY_MENU"
    else -> null
}

/** Tiny arithmetic parser for expressions such as `10 + 2 * dp(5)`. */
private class ExprParser(private val s: String, private val density: Float) {
    private var i = 0

    fun parse(): Float {
        val v = expr()
        skip()
        if (i < s.length) error("Unexpected trailing input")
        return v
    }

    private fun skip() {
        while (i < s.length && s[i].isWhitespace()) i++
    }

    private fun peek(): Char? {
        skip()
        return if (i < s.length) s[i] else null
    }

    private fun expect(c: Char) {
        if (peek() != c) error("Expected $c")
        i++
    }

    private fun expr(): Float {
        var v = term()
        while (true) {
            val c = peek()
            if (c == '+') {
                i++
                v += term()
            } else if (c == '-') {
                i++
                v -= term()
            } else {
                return v
            }
        }
    }

    private fun term(): Float {
        var v = factor()
        while (true) {
            val c = peek()
            if (c == '*') {
                i++
                v *= factor()
            } else if (c == '/') {
                i++
                val d = factor()
                v = if (d == 0f) 0f else v / d
            } else {
                return v
            }
        }
    }

    private fun factor(): Float {
        val c = peek() ?: error("Unexpected end")
        if (c == '-') {
            i++
            return -factor()
        }
        if (c == '+') {
            i++
            return factor()
        }
        if (c == '(') {
            i++
            val v = expr()
            expect(')')
            return v
        }
        if (c.isLetter()) {
            val start = i
            while (i < s.length && (s[i].isLetterOrDigit() || s[i] == '_')) i++
            val name = s.substring(start, i)
            expect('(')
            val arg = expr()
            expect(')')
            return when (name) {
                "dp" -> arg / density
                "px" -> arg * density
                else -> error("Unknown function $name")
            }
        }
        return number()
    }

    private fun number(): Float {
        skip()
        val start = i
        while (i < s.length && (s[i].isDigit() || s[i] == '.')) i++
        if (i < s.length && (s[i] == 'e' || s[i] == 'E')) {
            var j = i + 1
            if (j < s.length && (s[j] == '+' || s[j] == '-')) j++
            if (j < s.length && s[j].isDigit()) {
                i = j
                while (i < s.length && s[i].isDigit()) i++
            }
        }
        return s.substring(start, i).toFloatOrNull() ?: error("Invalid number")
    }
}

private class Zalith1Converter(private val screen: Zalith1ReferenceScreen) {
    private val styles = LinkedHashMap<StyleKey, ButtonStyle>()
    private val varRegex = Regex("\\$\\{(\\w+)\\}")

    private fun evaluate(expr: String, wPx: Float, hPx: Float): Float {
        val vars = mapOf(
            "top" to 0f,
            "left" to 0f,
            "right" to screen.widthPx - wPx,
            "bottom" to screen.heightPx - hPx,
            "width" to wPx,
            "height" to hPx,
            "screen_width" to screen.widthPx,
            "screen_height" to screen.heightPx,
            "margin" to 2f * screen.density,
            "preferred_scale" to 100f
        )
        val substituted = varRegex.replace(expr) { m ->
            "(" + (vars[m.groupValues[1]] ?: 0f).toString() + ")"
        }
        return try {
            ExprParser(substituted, screen.density).parse()
        } catch (_: Exception) {
            0f
        }
    }

    private fun toPercent(px: Float, total: Float, size: Float): Int {
        val available = total - size
        return if (available <= 0f) 0 else ((px / available) * 10000f).roundToInt().coerceIn(0, 10000)
    }

    private fun styleFor(b: LegacyButton, wDp: Float, hDp: Float): ButtonStyle {
        val radius = (b.cornerRadius.coerceIn(0f, 100f) / 100f * (min(wDp, hDp) / 2f)).coerceIn(0f, 100f)
        val key = StyleKey(
            bg = b.bgColor,
            stroke = b.strokeColor,
            strokeWidth = b.strokeWidth.roundToInt().coerceIn(0, 50),
            radius = (radius * 10f).roundToInt() / 10f,
            alpha = (b.opacity.coerceIn(0f, 1f) * 100f).roundToInt() / 100f
        )
        return styles.getOrPut(key) {
            val shape = ButtonShape(key.radius)
            val cfg = ButtonStyle.StyleConfig(
                alpha = key.alpha,
                pressedAlpha = key.alpha,
                backgroundColor = Color(key.bg),
                pressedBackgroundColor = Color.Gray.copy(alpha = 0.7f),
                contentColor = Color.White,
                pressedContentColor = Color.White,
                fontSize = null,
                pressedFontSize = null,
                borderWidth = key.strokeWidth,
                pressedBorderWidth = key.strokeWidth,
                borderColor = Color(key.stroke),
                pressedBorderColor = Color(key.stroke),
                borderRadius = shape,
                pressedBorderRadius = shape
            )
            ButtonStyle(
                name = "Zalith 1 style ${styles.size + 1}",
                uuid = randomUUID(),
                animateSwap = false,
                commonStyle = true,
                lightStyle = cfg,
                darkStyle = cfg
            )
        }
    }

    private fun buildEvents(b: LegacyButton, toggleTargetLayer: String?): List<ClickEvent> {
        val events = mutableListOf<ClickEvent>()
        for (code in b.keycodes) {
            when {
                code == -1 -> {
                    if (b.name.contains("keyboard", ignoreCase = true)) {
                        events += ClickEvent(ClickEvent.Type.LauncherEvent, EVENT_SWITCH_IME)
                    }
                }
                code == -2 -> {
                    if (toggleTargetLayer != null) {
                        events += ClickEvent(ClickEvent.Type.SwitchLayer, toggleTargetLayer)
                    }
                }
                code == -3 -> events += ClickEvent(ClickEvent.Type.LauncherEvent, "GLFW_MOUSE_BUTTON_LEFT")
                code == -4 -> events += ClickEvent(ClickEvent.Type.LauncherEvent, "GLFW_MOUSE_BUTTON_RIGHT")
                code == -6 -> events += ClickEvent(ClickEvent.Type.LauncherEvent, "GLFW_MOUSE_BUTTON_MIDDLE")
                code == -7 -> events += ClickEvent(ClickEvent.Type.LauncherEvent, EVENT_SCROLL_UP)
                code == -8 -> events += ClickEvent(ClickEvent.Type.LauncherEvent, EVENT_SCROLL_DOWN)
                code == -9 -> events += ClickEvent(ClickEvent.Type.LauncherEvent, EVENT_SWITCH_MENU)
                code >= 0 -> glfwKeyName(code)?.let { events += ClickEvent(ClickEvent.Type.Key, it) }
            }
        }
        return events.distinct()
    }

    private fun toNormal(
        b: LegacyButton,
        events: List<ClickEvent>,
        xPx: Float,
        yPx: Float
    ): NormalData {
        val wDp = max(b.widthDp, 5f)
        val hDp = max(b.heightDp, 5f)
        val wPx = wDp * screen.density
        val hPx = hDp * screen.density
        val style = styleFor(b, wDp, hDp)
        val visibility = when {
            b.displayInGame && !b.displayInMenu -> VisibilityType.IN_GAME
            !b.displayInGame && b.displayInMenu -> VisibilityType.IN_MENU
            else -> VisibilityType.ALWAYS
        }
        return NormalData(
            text = TranslatableString(b.name, emptyList()),
            uuid = randomUUID(18),
            position = ButtonPosition(
                x = toPercent(xPx, screen.widthPx, wPx),
                y = toPercent(yPx, screen.heightPx, hPx)
            ),
            buttonSize = DefaultSize.copy(
                type = ButtonSize.Type.Dp,
                widthDp = wDp,
                heightDp = hDp
            ),
            buttonStyle = style.uuid,
            textAlignment = TextAlignment.Center,
            visibilityType = visibility,
            _clickEvents = events,
            isSwipple = b.isSwipeable,
            isPenetrable = b.passThru,
            isToggleable = b.isToggle
        )
    }

    private fun evalPosition(b: LegacyButton): Pair<Float, Float> {
        val wPx = max(b.widthDp, 5f) * screen.density
        val hPx = max(b.heightDp, 5f) * screen.density
        return evaluate(b.dynamicX, wPx, hPx) to evaluate(b.dynamicY, wPx, hPx)
    }

    fun convert(json: JsonObject): ControlLayout {
        val mainLayerUuid = randomUUID()
        val mainButtons = mutableListOf<NormalData>()
        val toggleButtons = mutableListOf<NormalData>()
        val drawerLayers = mutableListOf<ControlLayer>()

        // Normal buttons
        (json["mControlDataList"] as? JsonArray)?.forEach { el ->
            val obj = el as? JsonObject ?: return@forEach
            val b = obj.toLegacyButton()
            val (x, y) = evalPosition(b)
            if (b.keycodes.contains(-2)) {
                toggleButtons += toNormal(b, buildEvents(b, mainLayerUuid), x, y)
            } else {
                mainButtons += toNormal(b, buildEvents(b, null), x, y)
            }
        }

        // Drawers: the drawer button toggles a hidden layer that holds its children
        (json["mDrawerDataList"] as? JsonArray)?.forEach { el ->
            val obj = el as? JsonObject ?: return@forEach
            val props = (obj["properties"] as? JsonObject)?.toLegacyButton() ?: return@forEach
            val children = (obj["buttonProperties"] as? JsonArray)
                ?.mapNotNull { (it as? JsonObject)?.toLegacyButton() }
                ?: emptyList()
            val orientation = obj.optStr("orientation") ?: "LEFT"

            val drawerLayerUuid = randomUUID()
            val dW = max(props.widthDp, 5f) * screen.density
            val dH = max(props.heightDp, 5f) * screen.density
            val (dX, dY) = evalPosition(props)

            mainButtons += toNormal(
                props,
                listOf(ClickEvent(ClickEvent.Type.SwitchLayer, drawerLayerUuid)),
                dX,
                dY
            )

            val childButtons = mutableListOf<NormalData>()
            var offset = 0f
            for (child in children) {
                val cW = max(child.widthDp, 5f) * screen.density
                val cH = max(child.heightDp, 5f) * screen.density
                val x: Float
                val y: Float
                when (orientation) {
                    "DOWN" -> {
                        x = dX
                        y = dY + dH + offset
                        offset += cH
                    }
                    "UP" -> {
                        offset += cH
                        x = dX
                        y = dY - offset
                    }
                    "RIGHT" -> {
                        x = dX + dW + offset
                        y = dY
                        offset += cW
                    }
                    "LEFT" -> {
                        offset += cW
                        x = dX - offset
                        y = dY
                    }
                    else -> {
                        val p = evalPosition(child)
                        x = p.first
                        y = p.second
                    }
                }
                childButtons += toNormal(child, buildEvents(child, null), x, y)
            }

            drawerLayers += ControlLayer(
                name = props.name,
                uuid = drawerLayerUuid,
                hide = true,
                hideWhenMouse = true,
                hideWhenGamepad = true,
                visibilityType = VisibilityType.ALWAYS,
                normalButtons = childButtons
            )
        }

        val layers = mutableListOf<ControlLayer>()
        layers += ControlLayer(
            name = "Zalith 1",
            uuid = mainLayerUuid,
            hide = false,
            hideWhenMouse = true,
            hideWhenGamepad = true,
            visibilityType = VisibilityType.ALWAYS,
            normalButtons = mainButtons
        )
        layers += drawerLayers
        if (toggleButtons.isNotEmpty()) {
            layers += ControlLayer(
                name = "GUI",
                uuid = randomUUID(),
                hide = false,
                hideWhenMouse = true,
                hideWhenGamepad = true,
                visibilityType = VisibilityType.ALWAYS,
                normalButtons = toggleButtons
            )
        }

        val info = json["mControlInfoDataList"] as? JsonObject
        fun field(key: String, fallback: String): String =
            info?.optStr(key)?.takeIf { it.isNotBlank() && it != "null" } ?: fallback

        return ControlLayout(
            info = ControlLayout.Info(
                name = TranslatableString(field("name", "Zalith 1 layout"), emptyList()),
                author = TranslatableString(field("author", ""), emptyList()),
                description = TranslatableString(field("desc", ""), emptyList()),
                versionCode = 1,
                versionName = field("version", "1")
            ),
            layers = layers,
            styles = styles.values.toList(),
            joystickStyles = emptyList(),
            editorVersion = EDITOR_VERSION
        )
    }
}
