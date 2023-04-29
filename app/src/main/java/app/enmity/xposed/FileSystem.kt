package app.enmity.xposed


class FileSystem {
    companion object {
        fun getAsset(file: String): String {
            val stream = Main.resources.assets.open(file)
            return stream.bufferedReader().use { it.readText() }
        }
    }
}