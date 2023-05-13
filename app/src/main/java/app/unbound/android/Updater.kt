package app.unbound.android

class Updater {
    companion object {
        fun hasUpdate(): Boolean {
//            return false
            if (Unbound.settings.get("unbound", "loader.update.force", false) as Boolean) {
                return true
            }

            // TODO: Implement HEAD ETag fetch logic here
            return true
        }

        fun getDownloadURL(): String {
            return Unbound.settings.get("unbound", "loader.update.url", "http://192.168.0.35:8080/bundle.js") as String
        }
    }
}