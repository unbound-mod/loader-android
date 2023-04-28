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
    override fun initZygote(startupParam: IXposedHookZygoteInit.StartupParam) {
        FileSystem.setResources(XModuleResources.createInstance(startupParam.modulePath, null))
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
        )

        val loadScriptFromAssets = instance.getDeclaredMethod(
            "jniLoadScriptFromAssets",
            AssetManager::class.java,
            String::class.java,
            Boolean::class.javaPrimitiveType
        )

        XposedBridge.hookMethod(loadScriptFromFile, object : XC_MethodHook() {
            override fun afterHookedMethod(param: MethodHookParam) {
                super.afterHookedMethod(param)

                Log.i("Enmity", "Called loadScriptFromFile")
            }
        })
        XposedBridge.hookMethod(loadScriptFromAssets, object : XC_MethodHook() {
            override fun afterHookedMethod(param: MethodHookParam) {
                super.afterHookedMethod(param)

                Log.i("Enmity", "Called loadScriptFromAssets")
            }
        })

        return@with
    }

}