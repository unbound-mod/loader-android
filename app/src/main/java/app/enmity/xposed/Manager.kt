package app.enmity.xposed

import android.util.Log
import java.io.File

open class Manager {
    private var addons: MutableList<Addon> = mutableListOf()

    fun initialize() {
        val folder = File(Enmity.fs.path, this.getType()).also { it.mkdirs() }

        val files = folder.list()

        for (file in files!!) {
            try {
                val path = File(folder, file)
                if (!path.isDirectory) {
                    Log.i("Enmity", "[${this.getType()}] Skipping $file as it is not a directory.")
                    continue
                }

                val deletion = File(path, ".delete")
                if (deletion.exists()) {
                    Log.i("Enmity", "[${this.getType()}] Deleting $file as it's pending deletion.")
                    path.deleteRecursively()
                    continue
                }

                val manifest = File(path, "manifest.json")
                if (!manifest.exists()) {
                    Log.i("Enmity", "[${this.getType()}] Skipping $file as it does not have a manifest.")
                    continue
                }

                val bundle = File(path, "bundle" + this.getExtension())
                if (!bundle.exists()) {
                    Log.i("Enmity", "[${this.getType()}] Skipping $file as its bundle does not exist.")
                    continue
                }

                val json = Enmity.gson.fromJson(manifest.readText(), Manifest::class.java)
                val payload = this.handleBundle(bundle.readText())
                val addon = Addon(payload, json)

                addons.add(addon)
            } catch(e: Exception) {
                Log.wtf("Enmity", "Error loading $file: $e")
            }
        }
    }

    fun getAddons(): String {
        return Enmity.gson.toJson(addons)
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