package app.unbound.android

import android.util.Log
import java.net.HttpURLConnection
import java.net.URL

class Updater {
    companion object {
        var etag: String = ""

        fun hasUpdate(): Boolean {
            Log.i("Unbound", "Checking for updates...")

            if (Unbound.settings.get("unbound", "loader.update.force", false) as Boolean) {
                Log.i("Unbound", "[Updater] Forcing update due to config. Forcing update due to config.")
                return true
            }

            try {
                val url = URL(getDownloadURL())
                val connection = url.openConnection() as HttpURLConnection

                with (connection) {
                    defaultUseCaches = false
                    useCaches = false
                    requestMethod = "HEAD"
                    connectTimeout = 2000
                    readTimeout = 2000
                }

                if (connection.responseCode != 200) {
                    throw Error("Bundle request failed with status ${connection.responseCode}")
                }

                val tag = Unbound.settings.get("unbound", "loader.update.etag", null)
                val header = connection.getHeaderField("etag")

                val res = header != tag
                if (res) {
                    Log.i("Unbound", "Detected new update.")
                } else {
                    Log.i("Unbound", "No updates found.")
                }

                etag = header

                return res
            } catch (e: Exception) {
                Log.i("Unbound", "No updates found as the server failed to respond with a valid ETag.")
                Utilities.alert("Failed to check for updates, bundle may be out of date. Please report this to the developers.")
                return false
            }
        }

        fun getDownloadURL(): String {
            return Unbound.settings.get("unbound", "loader.update.url", "http://192.168.0.35:8080/bundle.js") as String
        }
    }
}