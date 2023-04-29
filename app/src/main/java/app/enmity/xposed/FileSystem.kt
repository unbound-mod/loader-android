package app.enmity.xposed

import android.content.res.XModuleResources
import app.enmity.xposed.Main
import android.util.Log

class FileSystem {
    companion object {
        fun getAsset(file: String): String {
            val stream = Main.Companion.resources.assets.open(file)
            return stream.bufferedReader().use { it.readText() }
        }
    }
}