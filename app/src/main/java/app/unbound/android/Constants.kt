package app.unbound.android

import com.google.gson.JsonElement
import com.google.gson.annotations.SerializedName

data class Addon (
    val bundle: Any,
    val manifest: Manifest
)

data class Theme (
    val bundle: ThemeJSON,
    val manifest: Manifest
)

data class ThemeJSON (
    @SerializedName("raw") val raw: JsonElement?,
    @SerializedName("semantic") val semantic: JsonElement?,
    @SerializedName("background") val background: JsonElement?
)

data class Manifest (
    @SerializedName("name") var name : String,
    @SerializedName("id") var id : String,
    @SerializedName("description") var description : String,
    @SerializedName("version") var version : String,
    @SerializedName("authors") var authors : ArrayList<Authors>,

    @SerializedName("bundle") var bundle : String
)

data class Authors (
    @SerializedName("name") var name : String,
    @SerializedName("id") var id : String
)

class Constants {
    companion object {
        const val CLASS = "com.facebook.react.bridge.CatalystInstanceImpl"
        const val ACTIVITY_CLASS = "android.app.Instrumentation"
        const val LIGHT_THEME = "com.discord.theme.LightTheme"
        const val DARK_THEME = "com.discord.theme.DarkTheme"

        const val FILE_LOAD = "jniLoadScriptFromFile"
        const val ASSET_LOAD = "jniLoadScriptFromAssets"
        const val NEW_ACTIVITY = "newActivity"
    }
}