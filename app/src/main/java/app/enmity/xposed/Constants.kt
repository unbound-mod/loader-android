package app.enmity.xposed

class Constants {
    companion object {
        const val CLASS = "com.facebook.react.bridge.CatalystInstanceImpl"
        const val FILE_LOAD = "loadScriptFromFile"
        const val ASSET_LOAD = "loadScriptFromAssets"
    }
}