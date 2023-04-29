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

class Main: IXposedHookZygoteInit, IXposedHookLoadPackage, IXposedHookInitPackageResources {
    companion object {
        lateinit var resources: XModuleResources
    }

    override fun initZygote(startupParam: IXposedHookZygoteInit.StartupParam) {
        resources = XModuleResources.createInstance(startupParam.modulePath, null)
    }

    override fun handleInitPackageResources(resparam: XC_InitPackageResources.InitPackageResourcesParam) {
        if (resparam.packageName == "com.google.android.webview") return
    }

    override fun handleLoadPackage(lpparam: XC_LoadPackage.LoadPackageParam) = with(lpparam) {
        if (packageName == "com.google.android.webview") return

        Log.i("Enmity", "Loaded into $packageName...")
        val instance = classLoader.loadClass("com.facebook.react.bridge.CatalystInstanceImpl")

        val loadScriptFromFile = instance.getDeclaredMethod(
            "jniLoadScriptFromFile",
            String::class.java,
            String::class.java,
            Boolean::class.javaPrimitiveType
        ).apply { isAccessible = true }

        val loadScriptFromAssets = instance.getDeclaredMethod(
            "jniLoadScriptFromAssets",
            AssetManager::class.java,
            String::class.java,
            Boolean::class.javaPrimitiveType
        ).apply { isAccessible = true }

        XposedBridge.hookMethod(loadScriptFromFile, object : XC_MethodHook() {
            override fun beforeHookedMethod(param: MethodHookParam) {
                try {
                    XposedBridge.invokeOriginalMethod(
                        loadScriptFromAssets,
                        param.thisObject,
                        arrayOf(resources.assets, "assets://js/modules.js", false)
                    )
                } catch (e: Throwable) {
                    Log.wtf("Enmity", "Failed to execute modules patch: " + e)
                }
            }
        })

        XposedBridge.hookMethod(loadScriptFromFile, object : XC_MethodHook() {
            override fun afterHookedMethod(param: MethodHookParam) {
                Log.i("Enmity", "Attempting to inject bundle...")

                try {
                    // TODO: Create module wrapped in __d and execute
                    // Utilities.createModule("alert('hi')", 9000)

                    XposedBridge.invokeOriginalMethod(
                        loadScriptFromAssets,
                        param.thisObject,
                        arrayOf(resources.assets, "assets://js/bundle.js", false)
                    )

                    Log.i("Enmity", "Bundle injected.")
                } catch (e: Throwable) {
                    Log.wtf("Enmity", "Failed to inject bundle: " + e)
                }
            }
        })

        return@with
    }

}