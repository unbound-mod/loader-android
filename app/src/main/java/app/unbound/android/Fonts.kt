package app.unbound.android

import android.graphics.Typeface
import android.widget.TextView
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedHelpers
import java.io.File
import java.net.URL


class Fonts {
    fun download(url: URL) {
        val folder = File(Unbound.fs.path, "Fonts")
        val filename = File(url.path).getName()
        val file = File(folder, filename)

        Unbound.fs.download(url, file)
    }

    init {
        XposedHelpers.findAndHookMethod(TextView::class.java, "setTypeface", Typeface::class.java, Int::class.javaPrimitiveType,  object : XC_MethodHook() {
            @Throws(Throwable::class)
            override fun beforeHookedMethod(param: MethodHookParam) {
                val font = param.args[0] as Typeface;

                val internalMap = font.javaClass.getDeclaredField("sSystemFontMap");
                internalMap.isAccessible = true

                val map = internalMap.get(font) as Map<String?, Typeface?>
                for ((key, value) in map.entries) {
//                    Log.d("Unbound-Fonts", key + " ---> " + value + "\n")
                }
            }
        })

        XposedHelpers.findAndHookMethod(TextView::class.java, "setTypeface", Typeface::class.java, object : XC_MethodHook() {
            @Throws(Throwable::class)
            override fun beforeHookedMethod(param: MethodHookParam) {
                val font = param.args[0] as Typeface;

                val internalMap = font.javaClass.getDeclaredField("sSystemFontMap");
                internalMap.isAccessible = true

                val map = internalMap.get(font) as Map<String?, Typeface?>
                for ((key, value) in map.entries) {
//                    Log.d("Unbound-Fonts", key + " ---> " + value + "\n")
                }
            }
        })
    }
}