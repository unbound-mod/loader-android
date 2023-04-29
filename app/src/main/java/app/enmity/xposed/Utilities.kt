package app.enmity.xposed

import android.util.Log
import app.enmity.xposed.Main

class Utilities {
    companion object {
        fun createModule(code: String, id: Number): String {
            val wrapper = FileSystem.getAsset("js/wrapper.js")

            return wrapper.replace(Regex("$\$id$$/g"), id.toString()).replace("$\$code$$", code)
        }
    }
}