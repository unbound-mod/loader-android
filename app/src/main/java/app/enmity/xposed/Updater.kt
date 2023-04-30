package app.enmity.xposed

import java.net.URL

class Updater {
    companion object {
        fun hasUpdate(): Boolean {
            if (Settings.get("enmity", "loader.update.force", false) as Boolean) {
                return true
            }

            return true
        }

        fun getDownloadURL(): String {
            return Settings.get("enmity", "loader.update.url", "https://raw.githubusercontent.com/enmity-mod/enmity/main/dist/bundle.js") as String
        }
    }
}