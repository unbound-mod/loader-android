package app.enmity.xposed

import com.google.gson.annotations.SerializedName

data class Addon (
    val bundle: Any,
    val manifest: Manifest
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
        const val FILE_LOAD = "loadScriptFromFile"
        const val ASSET_LOAD = "loadScriptFromAssets"
    }
}