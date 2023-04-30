package app.enmity.xposed

import android.content.pm.ApplicationInfo

class Themes {
    companion object {
        fun initialize(info: ApplicationInfo) {

        }

        fun getThemes(): String {
            return "[]"
        }
    }
}