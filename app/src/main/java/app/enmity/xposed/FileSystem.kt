package app.enmity.xposed

import android.content.res.XModuleResources

class FileSystem {
    companion object {
        private lateinit var resources: XModuleResources

        fun setResources(assets: XModuleResources) {
            resources = assets
        }

        fun getAsset(file: String): String {
            val asset = resources.assets.open(file)
            return asset.bufferedReader().use { it.readText() }
        }
    }
}