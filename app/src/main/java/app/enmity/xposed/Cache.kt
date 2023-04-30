package app.enmity.xposed

import android.content.pm.ApplicationInfo
import android.util.Log
import java.io.File
import kotlin.io.path.Path

class Cache {
    companion object {
        lateinit var path: String
        fun initialize(info: ApplicationInfo) = with(info) {
            path = File(dataDir, "cache").also { it.mkdirs() }.toString()
        }

        fun getFile(name: String): String {
            val file = File(path, name)

            return file.readText()
        }

        fun writeFile(name: String, content: String): String {
            val file = File(path, name)
            file.writeText(content)

            return file.path
        }

        fun purgeFile(name: String) {
            val file = File(path, name)
            file.delete()
        }

        fun purge() {
            val cache = File(path)
            cache.deleteRecursively()
        }
    }
}