package app.unbound.android

class Plugins : Manager() {
    init {
        this.initialize()
    }

    override fun getType(): String {
        return "Plugins"
    }
}