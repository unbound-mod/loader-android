package app.enmity.xposed

class Themes : Manager() {
    init {
        this.initialize()
    }

    override fun getType(): String {
        return "Themes"
    }

    override fun getExtension(): String {
        return ".json"
    }

    override fun handleBundle(bundle: String): Any {
        return Enmity.gson.fromJson(bundle, Object::class.java)
    }
}