package app.enmity.xposed

import android.content.pm.ApplicationInfo
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
import java.net.HttpURLConnection
import java.net.URL


class Enmity: IXposedHookZygoteInit, IXposedHookLoadPackage, IXposedHookInitPackageResources {
    companion object {
        var gson: Gson = GsonBuilder().setPrettyPrinting().create()

        lateinit var resources: XModuleResources
        lateinit var info: ApplicationInfo

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

        info = appInfo
        initialize(param)

        // Don't load bundle if not configured to do so.
        if (!(settings.get("enmity", "loader.enabled", true) as Boolean)) {
            Log.i("Enmity", "Loader is disabled, skipping injection.")
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

        XposedBridge.hookMethod(loadScriptFromFile, object : XC_MethodHook() {
            override fun beforeHookedMethod(param: MethodHookParam) {
                try {
                    Log.i("Enmity", "Attempting to execute modules patch...")

                    XposedBridge.invokeOriginalMethod(
                        loadScriptFromAssets,
                        param.thisObject,
                        arrayOf(resources.assets, "assets://js/modules.js", false)
                    )

                    Log.i("Enmity", "Executed modules patch.")
                } catch (e: Throwable) {
                    Log.wtf("Enmity", "Modules patch injection failed, expect issues. $e")
                }

                // Inject React DevTools if its enabled
                if ((settings.get("enmity", "loader.devtools", false) as Boolean)) {
                    try {
                        Log.i("Enmity", "Attempting to execute DevTools bundle...")

                        XposedBridge.invokeOriginalMethod(
                            loadScriptFromAssets,
                            param.thisObject,
                            arrayOf(resources.assets, "assets://js/devtools.js", true)
                        )

                        Log.i("Enmity", "Successfully executed DevTools bundle.")
                    } catch (e: Throwable) {
                        Log.wtf("Enmity", "Failed patch injection failed, expect issues. $e")
                    }
                }

                try {
                    Log.i("Enmity", "Pre-loading settings, plugins and themes...")
                    val bundle = Utilities.usePreload()

                    XposedBridge.invokeOriginalMethod(
                        loadScriptFromFile,
                        param.thisObject,
                        arrayOf(bundle, bundle, false)
                    )

                    Log.i("Enmity", "Pre-loaded settings, plugins and themes.")
                } catch (e: Throwable) {
                    Log.wtf("Enmity", e)
                    Log.wtf("Enmity", "Failed to pre-load settings, plugins and themes. $e")
                }
            }

            override fun afterHookedMethod(param: MethodHookParam) {
                val bundle = File(fs.path, "bundle.js")

                if (!bundle.exists() || Updater.hasUpdate()) {
                    try {
                        val url = URL(Updater.getDownloadURL())
                        val connection = url.openConnection() as HttpURLConnection

                        with (connection) {
                            defaultUseCaches = false
                            useCaches = false
                            connectTimeout = 2000
                            readTimeout = 2000
                        }

                        if (connection.responseCode != 200) {
                            throw Error("Bundle request failed with status ${connection.responseCode}")
                        }

                        val body = connection.inputStream.readBytes()

                        bundle.writeBytes(body)
                    } catch (e: Exception) {
                        Log.wtf("Enmity", "Failed to download bundle. $e")

                        if (!bundle.exists()) {
                            Utilities.alert("Failed to load Enmity's bundle. Please report this to the developers.")
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
                    Log.i("Enmity", "Attempting to execute bundle...")

                    XposedBridge.invokeOriginalMethod(
                        loadScriptFromFile,
                        param.thisObject,
                        arrayOf(bundle.path, bundle.path, false)
                    )

                    Log.i("Enmity", "Bundle successfully executed.")
                } catch (e: Throwable) {
                    Log.wtf("Enmity", "Failed to execute bundle. $e")
                    Utilities.alert("Failed to load Enmity's bundle. Please report this to the developers.")
                }

                Cache.purge()
            }
        })

        return@with
    }

    override fun initZygote(param: IXposedHookZygoteInit.StartupParam) {
        resources = XModuleResources.createInstance(param.modulePath, null)
    }

    override fun handleInitPackageResources(resparam: XC_InitPackageResources.InitPackageResourcesParam) {
        if (resparam.packageName == "com.google.android.webview") return
    }
}