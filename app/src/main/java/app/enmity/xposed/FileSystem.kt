package app.enmity.xposed

import android.content.pm.ApplicationInfo
import java.io.File


class FileSystem {
    companion object {
        lateinit var path: String
        fun getAsset(file: String): String {
            val stream = Enmity.resources.assets.open(file)
            return stream.bufferedReader().use { it.readText() }
        }

        /*
         * Prepare directories as RTNFileManager doesn't automatically create
         * non-existing directories on android
         */
        fun initialize(info: ApplicationInfo) {
            path = File(info.dataDir, "files").also { it.mkdirs() }.path

            val enmity = File(this.path, "Enmity").also { it.mkdirs() }

            File(enmity.path, "Plugins").also { it.mkdirs() }
            File(enmity.path, "Themes").also { it.mkdirs() }
        }
    }
}