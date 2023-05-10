package app.enmity.xposed

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.util.Log
import com.github.ajalt.colormath.Color
import com.github.ajalt.colormath.parseOrNull
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedBridge
import de.robv.android.xposed.XposedHelpers
import de.robv.android.xposed.callbacks.XC_LoadPackage
import java.lang.ref.WeakReference


class Utilities(param: XC_LoadPackage.LoadPackageParam) {
    init {
        with(param) {
            val instrumentation = XposedHelpers.findClass(Constants.ACTIVITY_CLASS, classLoader)

            val method = instrumentation.getMethod(
                Constants.NEW_ACTIVITY,
                ClassLoader::class.java,
                String::class.java,
                Intent::class.java
            )

            XposedBridge.hookMethod(method, Activities())
        }
    }

    companion object {
        fun alert(description: String) {
            try {
                val activity = Activities.current.get()
                if (activity == null) {
                    Log.wtf("Enmity", "Failed to alert: No hooked activity found.")
                    return
                }

                activity.runOnUiThread {
                    val alert = AlertDialog.Builder(activity)

                    alert.setTitle("Enmity")
                    alert.setMessage(description)

                    // TODO: Add Discord Server button & Ok
                    alert.setPositiveButton("Yes", null)
                    alert.setNegativeButton("No", null)

                    alert.show()
                }
            } catch(e: Exception) {
                Log.wtf("Enmity", "Failed to alert: $e")
            }
        }

        fun parseColor(color: String): Int? {
            val parsed = Color.parseOrNull(color) ?: return null

            return parsed.toSRGB().toRGBInt().argb.toInt()
        }
    }
}

class Activities : XC_MethodHook() {
    @Throws(Throwable::class)
    override fun afterHookedMethod(param: MethodHookParam) {
        /*
         * Prevent memory leak by encapsulating a weak reference to the activity,
         * allowing us to either retrieve a strong reference to an object, or return null,
         * if the activity was already destroyed by the memory manager.
         */
        current = WeakReference(param.result as Activity)
    }

    companion object {
        @Volatile
        lateinit var current: WeakReference<Activity?>
    }
}
