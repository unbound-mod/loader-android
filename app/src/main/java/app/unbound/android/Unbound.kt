package app.unbound.android

import android.content.res.AssetManager
import android.content.res.XModuleResources
import android.util.Log
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import de.robv.android.xposed.IXposedHookInitPackageResources
import de.robv.android.xposed.IXposedHookLoadPackage
import de.robv.android.xposed.IXposedHookZygoteInit
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedBridge
import de.robv.android.xposed.callbacks.XC_InitPackageResources
import de.robv.android.xposed.callbacks.XC_LoadPackage
import java.io.File


class Unbound: IXposedHookZygoteInit, IXposedHookLoadPackage, IXposedHookInitPackageResources {
    companion object {
        var gson: Gson = GsonBuilder().setPrettyPrinting().create()

        lateinit var resources: XModuleResources
        lateinit var info: XC_LoadPackage.LoadPackageParam

        lateinit var fs: FileSystem
        lateinit var settings: Settings
        lateinit var cache: Cache

        lateinit var plugins: Plugins
        lateinit var themes: Themes
        lateinit var utilities: Utilities
    }

    private fun initialize(param: XC_LoadPackage.LoadPackageParam) {
        fs = FileSystem()
        settings = Settings()
        cache = Cache()

        plugins = Plugins()
        themes = Themes()
        utilities = Utilities(param)
    }

    override fun handleLoadPackage(param: XC_LoadPackage.LoadPackageParam) = with(param) {
        if (packageName == "com.google.android.webview") return

        info = param
        initialize(param)

        // Don't load bundle if not configured to do so.
        if (!(settings.get("unbound", "loader.enabled", true) as Boolean)) {
            Log.i("Unbound", "Loader is disabled, skipping injection.")
            return
        }

        val instance = classLoader.loadClass(Constants.CLASS)

        val loadScriptFromFile = instance.getDeclaredMethod(
            Constants.FILE_LOAD,
            String::class.java,
            String::class.java,
            Boolean::class.javaPrimitiveType
        ).apply { isAccessible = true }

        val loadScriptFromAssets = instance.getDeclaredMethod(
            Constants.ASSET_LOAD,
            AssetManager::class.java,
            String::class.java,
            Boolean::class.javaPrimitiveType
        ).apply { isAccessible = true }

        val patch = object : XC_MethodHook() {
            override fun beforeHookedMethod(param: MethodHookParam) {
                try {
                    Log.i("Unbound", "Attempting to execute modules patch...")

                    XposedBridge.invokeOriginalMethod(
                        loadScriptFromAssets,
                        param.thisObject,
                        arrayOf(resources.assets, "assets://js/modules.js", false)
                    )

                    Log.i("Unbound", "Successfully executed modules patch.")
                } catch (e: Throwable) {
                    Log.wtf("Unbound", "Modules patch injection failed, expect issues. $e")
                }

                if ((settings.get("unbound", "loader.devtools", false) as Boolean)) {
                    try {
                        Log.i("Unbound", "Attempting to execute DevTools bundle...")

                        XposedBridge.invokeOriginalMethod(
                            loadScriptFromAssets,
                            param.thisObject,
                            arrayOf(resources.assets, "assets://js/devtools.js", false)
                        )

                        Log.i("Unbound", "Successfully executed DevTools bundle.")
                    } catch (e: Throwable) {
                        Log.wtf("Unbound", "React DevTools failed to initialize. $e")
                    }
                }

                try {
                    Log.i("Unbound", "Pre-loading settings, plugins and themes...")
                    val bundle = usePreload()

                    XposedBridge.invokeOriginalMethod(
                        loadScriptFromFile,
                        param.thisObject,
                        arrayOf(bundle, bundle, false)
                    )

                    Log.i("Unbound", "Pre-loaded settings, plugins and themes.")
                } catch (e: Throwable) {
                    Log.wtf("Unbound", "Failed to pre-load settings, plugins and themes. $e")
                }
            }

            override fun afterHookedMethod(param: MethodHookParam) {
                val bundle = File(fs.path, "bundle.js")

                if (!bundle.exists() || Updater.hasUpdate()) {
                    try {
                        val url = Updater.getDownloadURL()

                        Log.i("Unbound", "Downloading bundle...")
                        fs.download(url, bundle)
                        Unbound.settings.set("unbound", "loader.update.etag", Updater.etag)
                        Log.i("Unbound", "Bundle downloaded.")
                    } catch (e: Exception) {
                        Log.wtf("Unbound", "Failed to download bundle. $e")

                        if (!bundle.exists()) {
                            Utilities.alert("Bundle failed to download, please report this to the developers.")
                            Cache.purge()
                            return
                        } else {
                            Utilities.alert("Bundle failed to update, loading out of date bundle.")
                        }
                    }
                }

                if (!bundle.exists()) {
                    Utilities.alert("Bundle not found, please report this to the developers.")
                    Cache.purge()
                    return
                }

                try {
                    Log.i("Unbound", "Attempting to execute bundle...")

                    XposedBridge.invokeOriginalMethod(
                        loadScriptFromFile,
                        param.thisObject,
                        arrayOf(bundle.path, bundle.path, false)
                    )

                    Log.i("Unbound", "Bundle successfully executed.")
                } catch (e: Throwable) {
                    Log.wtf("Unbound", "Failed to execute bundle. $e")
                    Utilities.alert("Failed to load Unbound's bundle. Please report this to the developers.")
                }

                Cache.purge()
            }
        }

        XposedBridge.hookMethod(loadScriptFromAssets, patch)
        XposedBridge.hookMethod(loadScriptFromFile, patch)

        return@with
    }

    override fun initZygote(param: IXposedHookZygoteInit.StartupParam) {
        resources = XModuleResources.createInstance(param.modulePath, null)
    }

    override fun handleInitPackageResources(param: XC_InitPackageResources.InitPackageResourcesParam) = with (param) {
        if (packageName == "com.google.android.webview") return

        val isEnabled = settings.get("unbound", "loader.enabled", true) as Boolean
        val isInRecovery = settings.get("unbound", "recovery", false) as Boolean

        if (!isEnabled || isInRecovery) return

        Themes.raw.forEach { (key, value) ->
            try {
                res.setReplacement("com.discord", "color", key, value)
            } catch (e: Exception) {
                Log.wtf("Unbound", "No raw color found for $key")
            }
        }
    }

    fun usePreload(): String {
        var template = fs.getAsset("js/preload.js")

        template = template.replace("#settings#", settings.getSettings())
        template = template.replace("#plugins#", plugins.getAddons())
        template = template.replace("#themes#", themes.getAddons())

        return Cache.writeFile("preload.js", template)
    }
}