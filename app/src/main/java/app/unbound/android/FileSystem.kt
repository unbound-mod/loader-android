package app.unbound.android

import java.io.File
import java.net.HttpURLConnection
import java.net.URL


class FileSystem {
    var path: String

    fun getAsset(file: String): String {
        val stream = Unbound.resources.assets.open(file)
        return stream.bufferedReader().use { it.readText() }
    }

    fun download(url: String, file: File) {
        val url = URL(url)
        val connection = url.openConnection() as HttpURLConnection

        with (connection) {
            defaultUseCaches = false
            useCaches = false
            connectTimeout = 2000
            readTimeout = 2000
        }

        if (connection.responseCode != 200) {
            throw Error("Bundle request failed with status ${connection.responseCode}")
        }

        val body = connection.inputStream.readBytes()

        file.writeBytes(body)
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