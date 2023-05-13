package app.unbound.android

import android.util.Log
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedBridge

class Themes : Manager() {
    companion object {
        val raw = mutableMapOf<String, Int>()
    }

    init {
        this.initialize()

        val isEnabled = Unbound.settings.get("unbound", "loader.enabled", true) as Boolean
        val isInRecovery = Unbound.settings.get("unbound", "recovery", false) as Boolean

        if (isEnabled && !isInRecovery) {
            this.apply()
        }
    }

    override fun getType(): String {
        return "Themes"
    }

    override fun process(payload: Any, json: Manifest): Theme {
        return Theme(payload as ThemeJSON, json)
    }

    override fun getExtension(): String {
        return ".json"
    }

    override fun handleBundle(bundle: String): ThemeJSON {
        return Unbound.gson.fromJson(bundle, ThemeJSON::class.java)
    }

    private fun getApplied(): Theme? {
        val key = Unbound.settings.get("theme-states", "applied", null)
        if (key == "" || key !is String) return null

        val theme = this.addons.find { t -> (t as Theme).manifest.id == key }
        if (theme != null) {
            return theme as Theme
        }

        return null
    }

    private fun apply() {
        val addon = this.getApplied() ?: return

        if (addon.bundle.raw != null) {
            val colors = addon.bundle.raw.asJsonObject.entrySet()

            colors.forEach { (key, value) ->
                val color = Utilities.parseColor(value.asString) ?: return@forEach

                raw[key.lowercase()] = color
            }
        }

        if (addon.bundle.semantic != null) {
            val colors = addon.bundle.semantic.asJsonObject.entrySet()
            val loader = Unbound.info.classLoader

            val dark = loader.loadClass(Constants.DARK_THEME)
            val light = loader.loadClass(Constants.LIGHT_THEME)

            colors.forEach { (key, value) ->
                // Keyboard theming is not yet supported on android
                if (key == "KEYBOARD") return@forEach

                val color = value.asJsonArray

                val segments = key.split("_")
                val getter = segments.joinToString("") { it.lowercase().replaceFirstChar { it.uppercase() } }
                val method = "get$getter"

                color.forEachIndexed { index, v ->
                    try {
                        val string = color.get(index)
                        val parsed = Utilities.parseColor(string.asString) ?: return@forEachIndexed

                        when (index) {
                            0 -> this.swizzle(
                                dark,
                                method,
                                parsed
                            )

                            1 -> this.swizzle(
                                light,
                                method,
                                parsed
                            )
                        }
                    } catch (e: Exception) {
                        Log.wtf("Unbound", "Failed to apply theme color $key, $v")
                    }
                }
            }
        }
    }

    private fun swizzle(theme: Class<*>, method: String, value: Int) {
        val implementation = theme.getDeclaredMethod(method)

        XposedBridge.hookMethod(implementation, object : XC_MethodHook() {
            override fun beforeHookedMethod(param: MethodHookParam) {
                param.result = value
            }
        })
    }
}