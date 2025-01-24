package app.unbound.android

import android.util.Log
import java.net.URL
import javax.net.ssl.HttpsURLConnection

class Updater {
    companion object {
        var etag: String = ""

        fun hasUpdate(): Boolean {
            Log.i("Unbound", "[Updater] Checking for updates...")

            if (Unbound.settings.get("unbound", "loader.update.force", false) as Boolean) {
                Log.i("Unbound", "[Updater] Forcing update due to config. Forcing update due to config.")
                return true
            }

            try {
                val url = getDownloadURL()
                val connection = url.openConnection() as HttpsURLConnection

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
                    Log.i("Unbound", "[Updater] Detected new update.")
                } else {
                    Log.i("Unbound", "[Updater] No updates found.")
                }

                etag = header

                return res
            } catch (e: Exception) {
                Log.i("Unbound", "[Updater] No updates found as the server failed to respond with a valid ETag.")
                Utilities.alert("[Updater] Failed to check for updates, bundle may be out of date. Please report this to the developers.")
                return false
            }
        }

        fun getDownloadURL(): URL {
            val url = Unbound.settings.get("unbound", "loader.update.url", "https://raw.githubusercontent.com/unbound-mod/builds/refs/heads/main/unbound.js") as String

            return URL(url)
        }
    }
}