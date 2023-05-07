package app.enmity.xposed

import org.json.JSONObject
import java.io.File

class Settings {
    var settings: JSONObject
    var path: String

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

    init {
        val folder = File(Enmity.info.dataDir, "files")
        var enmity = File(folder, "Enmity")
        val file = File(enmity, "settings.json")

        path = file.path

        if (!file.exists()) reset()

        settings = try {
            val payload = file.readText()

//                Enmity.gson.fromJson(payload, Any::class.java)
            JSONObject(payload)
        } catch (e: Exception) {
            JSONObject("{}")
        }
    }
}