package app.enmity.xposed

class Utilities {
    companion object {
        fun usePreload(): String {
            var template = Enmity.fs.getAsset("js/preload.js")

            template = template.replace("#settings#", Enmity.settings.getSettings())
            template = template.replace("#plugins#", Enmity.plugins.getAddons())
            template = template.replace("#themes#", Enmity.themes.getAddons())

            return Cache.writeFile("preload.js", template)
        }
    }
}
