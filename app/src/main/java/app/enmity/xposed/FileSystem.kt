package app.enmity.xposed

import java.io.File


class FileSystem {
    var path: String

    fun getAsset(file: String): String {
        val stream = Enmity.resources.assets.open(file)
        return stream.bufferedReader().use { it.readText() }
    }

    /*
     * Prepare directories as RTNFileManager doesn't automatically create
     * non-existing directories on android
     */
    init {
        val files = File(Enmity.info.dataDir, "files").also { it.mkdirs() }.path

        this.path = File(files, "Enmity").also { it.mkdirs() }.path

        File(this.path, "Plugins").also { it.mkdirs() }
        File(this.path, "Themes").also { it.mkdirs() }
    }
}