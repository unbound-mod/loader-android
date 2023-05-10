package app.enmity.xposed

class Updater {
    companion object {
        fun hasUpdate(): Boolean {
            if (Enmity.settings.get("enmity", "loader.update.force", false) as Boolean) {
                return true
            }

            // TODO: Implement HEAD ETag fetch logic here
            return true
        }

        fun getDownloadURL(): String {
            return Enmity.settings.get("enmity", "loader.update.url", "http://192.168.0.35:8080/bundle.js") as String
        }
    }
}