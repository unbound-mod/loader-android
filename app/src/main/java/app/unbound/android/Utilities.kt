package app.unbound.android

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
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
                    Log.wtf("Unbound", "Failed to alert: No hooked activity found.")
                    return
                }

                activity.runOnUiThread {
                    val alert = AlertDialog.Builder(activity)

                    alert.setTitle("Unbound")
                    alert.setMessage(description)

                    alert.setPositiveButton("Okay", null)
                    alert.setNegativeButton("Discord Server") { _, _ ->
                        val link = Intent(Intent.ACTION_VIEW, Uri.parse(Constants.DISCORD_SERVER))
                        activity.startActivity(link)
                    }

                    alert.show()
                }
            } catch(e: Exception) {
                Log.wtf("Unbound", "Failed to alert: $e")
            }
        }

        fun parseColor(color: String, opacity: Float? = null): Int? {
            return Color.parseOrNull(color)?.toSRGB()?.let { srgb ->
                srgb.copy(alpha = (opacity ?: srgb.alpha).coerceIn(0f, 1f)).toRGBInt().argb.toInt()
            }
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
