package app.enmity.xposed

import java.io.File

class Cache {
    companion object {
        private var path: String = File(Enmity.info.dataDir, "cache").also { it.mkdirs() }.toString()

        fun writeFile(name: String, content: String): String {
            val file = File(path, name)
            file.writeText(content)

            return file.path
        }

        fun purge() {
            val cache = File(path)
            cache.deleteRecursively()
        }
    }
}