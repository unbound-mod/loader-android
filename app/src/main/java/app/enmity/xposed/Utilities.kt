package app.enmity.xposed

class Utilities {
    companion object {
        fun usePreload(): String {
            var template = FileSystem.getAsset("js/preload.js")

            template = template.replace("#settings#", Settings.getSettings())
            template = template.replace("#plugins#", Plugins.getPlugins())
            template = template.replace("#themes#", Themes.getThemes())

            return Cache.writeFile("preload.js", template)
        }
    }
}
