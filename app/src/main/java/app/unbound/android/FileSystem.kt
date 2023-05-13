package app.unbound.android

import java.io.File


class FileSystem {
    var path: String

    fun getAsset(file: String): String {
        val stream = Unbound.resources.assets.open(file)
        return stream.bufferedReader().use { it.readText() }
    }

    /*
     * Prepare directories as RTNFileManager doesn't automatically create
     * non-existing directories on android
     */
    init {
        val files = File(Unbound.info.appInfo.dataDir, "files").also { it.mkdirs() }.path

        this.path = File(files, "Unbound").also { it.mkdirs() }.path

        File(this.path, "Plugins").also { it.mkdirs() }
        File(this.path, "Themes").also { it.mkdirs() }
    }
}