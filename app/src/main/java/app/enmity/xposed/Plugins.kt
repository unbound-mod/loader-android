package app.enmity.xposed

import android.content.pm.ApplicationInfo

class Plugins {
    companion object {
        fun initialize(info: ApplicationInfo) {

        }

        fun getPlugins(): String {
            return "{}"
        }
    }
}