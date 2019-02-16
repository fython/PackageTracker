package info.papdt.express.helper.ui.items

import androidx.annotation.StringDef

class DetailsTwoLineItem {

    @get:ItemType
    var type: String? = null
        private set
    var content: String? = null
        private set
    var optional: String? = null

    @StringDef(TYPE_NAME, TYPE_NUMBER)
    @Retention(AnnotationRetention.SOURCE)
    annotation class ItemType()

    constructor(@ItemType type: String, content: String) {
        this.type = type
        this.content = content
    }

    constructor(@ItemType type: String, content: String, optional: String?) {
        this.type = type
        this.content = content
        this.optional = optional
    }

    companion object {

        const val TYPE_NAME = "name"
        const val TYPE_NUMBER = "number"

    }

}
