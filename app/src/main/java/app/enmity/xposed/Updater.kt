package app.enmity.xposed

class Updater {
    companion object {
        fun hasUpdate(): Boolean {
            if (Enmity.settings.get("enmity", "loader.update.force", false) as Boolean) {
                return true
            }

            return true
        }

        fun getDownloadURL(): String {
            return Enmity.settings.get("enmity", "loader.update.url", "https://raw.githubusercontent.com/enmity-mod/enmity/main/dist/bundle.js") as String
        }
    }
}