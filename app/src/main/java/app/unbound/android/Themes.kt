package app.unbound.android

import android.content.Context
import android.content.res.Resources
import android.util.Log
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedBridge

class Themes : Manager() {
    companion object {
        val raw = mutableMapOf<String, Int>()
        val semanticHooks = mutableListOf<XC_MethodHook.Unhook>()
        val stockThemes = arrayOf("dark", "darker", "midnight", "amoled", "light")
        var themeType: String? = null
    }

    init {
        this.initialize()

        val isEnabled = Unbound.settings.get("unbound", "loader.enabled", true) as Boolean
        val isInRecovery = Unbound.settings.get("unbound", "recovery", false) as Boolean

        if (isEnabled && !isInRecovery) {
            val updateTheme = Unbound.info.classLoader.loadClass("com.discord.theme.ThemeModule").getDeclaredMethod("updateTheme", String::class.java)
            XposedBridge.hookMethod(updateTheme, object : XC_MethodHook() { // Runs on startup
                override fun beforeHookedMethod(param: MethodHookParam) {
                    Log.d("Unbound", "[Themes] Hooked updateTheme! : ${param.args[0]}")

                    if (param.args[0] !in stockThemes) {
                        //doesnt function if you enable directly after adding | should impl https://developer.android.com/reference/android/os/FileObserver to update addons
                        val theme = getTheme(param.args[0] as String)
                        if (theme == null) { param.result = null; return }
                        themeType = theme.bundle.type?.asString

                        Log.d("Unbound", "[Themes] ${param.args[0]} is $themeType")

                        rawConstructor(theme.bundle.raw)
                        hookSemantic(theme.bundle.semantic)


                        /* Reimplement updater branch:
                            android.app.Activity r4 = r3.getCurrentActivity()
                            if (r4 == 0) goto L58 // return
                            com.discord.theme.a r0 = new com.discord.theme.a
                            r0.<init>()
                            r4.runOnUiThread(r0)
                        */
                        // Not sure if this is necessary but stock updateTheme does and we are preventing its execution
                        // a.run() does eventually call some updateUI functions so i assume its useful for live updating
                        val a = Unbound.info.classLoader.loadClass("com.discord.theme.a")
                        val constructor = a.getDeclaredConstructor(param.thisObject.javaClass)
                        val runnable = constructor.newInstance(param.thisObject) as Runnable

                        Activities.current.get()?.runOnUiThread(runnable)

                        param.result = null // Don't run stock updateTheme(), will crash if it gets a custom theme id
                        return
                    }

                    raw.clear() // Remove custom colouring
                    semanticHooks.forEach { it.unhook() }
                    themeType = null
                }
            })

            val themeManager = Unbound.info.classLoader.loadClass("com.discord.theme.ThemeManager")
            fun hookIsThemeMethods(methodName: String, expectedType: String) {
                XposedBridge.hookMethod(themeManager.getDeclaredMethod(methodName), object : XC_MethodHook() {
                    override fun beforeHookedMethod(param: MethodHookParam) {
                        val buh = themeType == expectedType
                        Log.d("Unbound", "$methodName called: $themeType, $buh")
                        if (themeType != null) param.result = buh
                    }
                })
            }
            hookIsThemeMethods("isThemeLight", "light")
            hookIsThemeMethods("isThemeDark", "dark")

            hookRaw()
            apply()
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

    private fun getTheme(key: String): Theme? {
        val theme = this.addons.find { t -> (t as Theme).manifest.id == key }
        Log.d("Unbound", "[Themes] Applied theme: $theme")
        return theme as? Theme
    }

    private fun apply() {
        val key = Unbound.settings.get("themes", "applied", null)
        if (key == "" || key !is String) return

        getTheme(key)?.let { addon ->
            rawConstructor(addon.bundle.raw)
        }
    }

    private fun rawConstructor(addon: JsonElement?) {
        raw.clear()
        addon?.asJsonObject?.entrySet()?.forEach { (key, value) ->
            val color = Utilities.parseColor(value.asString)
            if (color != null) {
                raw[key.lowercase()] = color
            } else {
                Log.w("Unbound", "[Themes] Failed to parse raw color: $key")
            }
        }
    }
    private fun hookRaw() {
        val colorUtils = Unbound.info.classLoader.loadClass("com.discord.theme.utils.ColorUtilsKt")
        val getColorCompatLegacy = colorUtils.getDeclaredMethod("getColorCompat", Resources::class.java, Int::class.javaPrimitiveType, Resources.Theme::class.java)
        val getColorCompat = colorUtils.getDeclaredMethod("getColorCompat", Context::class.java, Int::class.javaPrimitiveType)

        val patch = object : XC_MethodHook() {
            override fun beforeHookedMethod(param: MethodHookParam) {
                val resources = (param.args[0] as? Context)?.resources ?: (param.args[0] as Resources)
                val name = resources.getResourceEntryName(param.args[1] as Int)
//                Log.d("Unbound", "[Themes] Swizzling raw: $name")

//                raw[name]?.let {
                if (raw[name] != null) {
                    Log.d("Unbound", "[Themes] Swizzled raw: $name, ${raw[name]}")
                    param.result = raw[name] //it
                } //else {
//                        Log.d("Unbound", "[Themes] Swizzled unset raw: $name, ${raw[name]}, #fc03f8")
//                        param.result = Utilities.parseColor("#fc03f8")
//                }
            }
        }

        XposedBridge.hookMethod(getColorCompat, patch)
        XposedBridge.hookMethod(getColorCompatLegacy, patch)
    }

    private fun hookSemantic(addon: JsonElement?) {
        semanticHooks.forEach { it.unhook() } // Remove all previous hooks to let new themes do their thing

        val getTheme = try {
            Unbound.info.classLoader
                .loadClass("com.discord.theme.ThemeManagerKt")
                .getDeclaredMethod("getTheme")
        } catch (e: Exception) {
            Log.e("Unbound", "[Themes] failed to retrieve getTheme(): $e")
            return
        }

//        val unwantedMethods = arrayOf("getClass", "getColor", "getColorRes")
//        for (method in themeClass.methods) {
//            Log.d("Unbound", "[Themes] Method: ${method.name}, Parameters: ${method.parameterTypes.joinToString()}")
//            if (method.name.startsWith("get") && method.name !in unwantedMethods) {
//                semanticSwizzle(themeClass, method.name, Utilities.parseColor("#f0f"), "key")
//            }
//        }

        addon?.asJsonObject?.entrySet()?.forEach { (key, json) ->
            val obj = json.asJsonObject

            val themeClass = getTheme.invoke(null)::class.java

            val segments = key.split("_")
            val getterMethod = "get" + segments.joinToString("") { it.lowercase().replaceFirstChar(Char::uppercase) }


            if (obj.get("type").asString == "color") {
                val colorValue = obj.get("value").asString
                val colorOpacity = obj.get("opacity")?.asFloat

                val color = Utilities.parseColor(colorValue, colorOpacity)

                semanticSwizzle(themeClass, getterMethod, color, key)

            } else if (obj.get("type").asString == "raw") {
                // Unimplemented
//                val rawKey = obj.get("value").asString
//                val colorOpacity = obj.get("opacity")?.asFloat
                return
            }
        }
    }
    private fun semanticSwizzle(theme: Class<*>, method: String, value: Int?, key: String) {
        try {
            if (value != null) {
                Log.d("Unbound", "[Themes] Applying semantic $key")
                val implementation = theme.getDeclaredMethod(method)
                val hook = XposedBridge.hookMethod(implementation, object : XC_MethodHook() {
                    override fun beforeHookedMethod(param: MethodHookParam) {
                        param.result = value
                    }
                })
                semanticHooks.add(hook)
            } else { throw IllegalArgumentException("value parsed to null.") }
        } catch (e: NoSuchMethodException) { // Common as most semantic strings aren't native
            Log.w("Unbound", "[Themes] $key is not available on native")
        } catch (e: Exception) {
            Log.e("Unbound", "[Themes] Error applying theme color $key", e)
        }
    }
}