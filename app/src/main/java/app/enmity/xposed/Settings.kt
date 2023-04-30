package app.enmity.xposed

import android.content.pm.ApplicationInfo
import android.util.Log
import org.json.JSONObject
import java.io.File

class Settings {
    companion object {
        lateinit var settings: JSONObject
        lateinit var path: String

        fun get(store: String, key: String, default: Any): Any {
            try {
                val payload = settings.getJSONObject(store)

                val keys = key.split(".");
                var result: Any = payload

                for (key in keys) {
                    val res = (result as JSONObject).get(key)
                    result = res
                }

                return result
            } catch (e: Exception) {
                return default
            }
        }

        private fun reset() {
            val settings = File(this.path)
            settings.writeText("{}")
        }

        fun getSettings(): String {
            return settings.toString(2)
        }

        fun initialize(info: ApplicationInfo) = with(info) {
            val folder = File(dataDir, "files")
            var enmity = File(folder, "Enmity")
            val file = File(enmity, "settings.json")

            path = file.path

            if (!file.exists()) reset()

            settings = try {
                val payload = file.readText()
                JSONObject(payload)
            } catch (e: Exception) {
                JSONObject("{}")
            }
        }
    }
}