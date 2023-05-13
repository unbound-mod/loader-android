package app.unbound.xposed

class Plugins : Manager() {
    init {
        this.initialize()
    }

    override fun getType(): String {
        return "Plugins"
    }
}