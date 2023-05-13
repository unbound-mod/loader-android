package app.unbound.xposed

import android.util.Log
import java.io.File

open class Manager {
    var addons: MutableList<Any> = mutableListOf()

    fun initialize() {
        val folder = File(Unbound.fs.path, this.getType()).also { it.mkdirs() }

        val files = folder.list()

        for (file in files!!) {
            try {
                val path = File(folder, file)
                if (!path.isDirectory) {
                    Log.i("Unbound", "[${this.getType()}] Skipping $file as it is not a directory.")
                    continue
                }

                val deletion = File(path, ".delete")
                if (deletion.exists()) {
                    Log.i("Unbound", "[${this.getType()}] Deleting $file as it's pending deletion.")
                    path.deleteRecursively()
                    continue
                }

                val manifest = File(path, "manifest.json")
                if (!manifest.exists()) {
                    Log.i("Unbound", "[${this.getType()}] Skipping $file as it does not have a manifest.")
                    continue
                }

                val bundle = File(path, "bundle" + this.getExtension())
                if (!bundle.exists()) {
                    Log.i("Unbound", "[${this.getType()}] Skipping $file as its bundle does not exist.")
                    continue
                }

                val json = Unbound.gson.fromJson(manifest.readText(), Manifest::class.java)
                val payload = this.handleBundle(bundle.readText())
                val addon = this.process(payload, json)

                addons.add(addon)
            } catch(e: Exception) {
                Log.wtf("Unbound", "Error loading $file: $e")
            }
        }
    }

    fun getAddons(): String {
        return Unbound.gson.toJson(addons)
    }

    open fun process(payload: Any, json: Manifest): Any {
        return Addon(payload, json)
    }

    open fun handleBundle(bundle: String): Any {
        return bundle
    }

    open fun getExtension(): String {
        return ".js"
    }

    open fun getType(): String {
        return "Manager"
    }
}