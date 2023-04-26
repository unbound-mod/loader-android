package app.enmity.xposed

import android.content.res.AssetManager
import android.content.res.XModuleResources
import android.util.Log
import de.robv.android.xposed.IXposedHookInitPackageResources
import de.robv.android.xposed.IXposedHookLoadPackage
import de.robv.android.xposed.IXposedHookZygoteInit
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedBridge
import de.robv.android.xposed.callbacks.XC_InitPackageResources
import de.robv.android.xposed.callbacks.XC_LoadPackage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File
import java.net.HttpURLConnection
import java.net.URL

class Main: IXposedHookZygoteInit, IXposedHookLoadPackage, IXposedHookInitPackageResources {
    private lateinit var moduleResources: XModuleResources

    override fun initZygote(startupParam: IXposedHookZygoteInit.StartupParam) {
        moduleResources = XModuleResources.createInstance(startupParam.modulePath, null)
    }

    override fun handleInitPackageResources(resparam: XC_InitPackageResources.InitPackageResourcesParam) {
        if(resparam.packageName == "com.google.android.webview") return
    }

    override fun handleLoadPackage(lpparam: XC_LoadPackage.LoadPackageParam) = with(lpparam) {
        if(packageName == "com.google.android.webview") return

        Log.i("Enmity", "Loaded into $packageName...")

        val catalystInstanceImpl = classLoader.loadClass("com.facebook.react.bridge.CatalystInstanceImpl")

        val jniLoadScriptFromAssets = catalystInstanceImpl.getDeclaredMethod(
            "jniLoadScriptFromAssets",
            AssetManager::class.java,
            String::class.java,
            Boolean::class.javaPrimitiveType
        )

        val jniLoadScriptFromFile = catalystInstanceImpl.getDeclaredMethod(
            "jniLoadScriptFromFile",
            String::class.java,
            String::class.java,
            Boolean::class.javaPrimitiveType
        )

        val cache = File(appInfo.dataDir, "cache").also { it.mkdirs() }
        val enmity = File(cache, "enmity.js")
        val wrapper = File(cache, "wrapper.js")

        val hook = object: XC_MethodHook() {
            override fun beforeHookedMethod(param: MethodHookParam) {
                val url = "INSERT_BUNDLE_URL_HERE"

                try {
                    val req = URL(url).openConnection() as HttpURLConnection

                    req.connectTimeout = 3000
                    req.readTimeout = 3000

                    if (req.responseCode == 200) {
                        val wrapperText = String(moduleResources.assets.open("js/wrapper.js").readBytes())
                        req.inputStream.use { inputStream ->
                            enmity.outputStream().use { outputStream ->
                                inputStream.copyTo(outputStream)
                            }
                        }

                        wrapper.outputStream().use {
                            val content = wrapperText.replaceFirst("$\$code$$", String(enmity.inputStream().readBytes()))
                            it.write(content.toByteArray())
                        }
                    }
                } catch (e: Throwable) {
                    Log.e("Enmity", "Failed to load enmity", e)
                }

                XposedBridge.invokeOriginalMethod(jniLoadScriptFromAssets, param.thisObject, arrayOf(moduleResources.assets, "assets://js/modules.js", true))
                XposedBridge.invokeOriginalMethod(jniLoadScriptFromAssets, param.thisObject, arrayOf(moduleResources.assets, "assets://js/identity.js", true))
            }

            override fun afterHookedMethod(param: MethodHookParam) {
                try {
                    CoroutineScope(Dispatchers.IO).launch {
                        delay(1000)
                        Log.i("Enmity", "Executing the wrapper...")
                        XposedBridge.invokeOriginalMethod(jniLoadScriptFromFile, param.thisObject, arrayOf(wrapper.absolutePath, wrapper.absolutePath, true))
                    }
                } catch (_: Throwable) {}
            }
        }

        XposedBridge.hookMethod(jniLoadScriptFromAssets, hook)
        XposedBridge.hookMethod(jniLoadScriptFromFile, hook)

        return@with
    }

}