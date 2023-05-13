package app.unbound.android

import org.json.JSONObject
import java.io.File

class Settings {
    private var settings: JSONObject
    private var path: String

    fun get(store: String, key: String, default: Any?): Any? {
        return try {
            val payload = settings.getJSONObject(store)

            val keys = key.split(".")
            var result: Any = payload

            @Suppress("NAME_SHADOWING")
            for (key in keys) {
                val res = (result as JSONObject).get(key)
                result = res
            }

            result
        } catch (e: Exception) {
            default
        }
    }

    fun set(store: String, key: String, value: Any) {
        val payload = settings.getJSONObject(store)

        val keys = key.split(".")
        var result: Any = payload

        @Suppress("NAME_SHADOWING")
        for (key in keys) {
            if (keys.indexOf(key) + 1 == keys.size) {
                (result as JSONObject).put(key, value)
                continue
            }

            result = try {
                (result as JSONObject).get(key)
            } catch (e: Exception) {
                (result as JSONObject).put(key, {})
            }
        }

        this.save()
    }

    private fun reset() {
        val settings = File(this.path)
        settings.writeText("{}")
    }

    private fun save() {
        val settings = File(this.path)
        settings.writeText(getSettings())
    }

    fun getSettings(): String {
        return settings.toString(2)
    }

    init {
        val folder = File(Unbound.info.appInfo.dataDir, "files")
        val unbound = File(folder, "Unbound").also { it.mkdirs() }
        val file = File(unbound, "settings.json")

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