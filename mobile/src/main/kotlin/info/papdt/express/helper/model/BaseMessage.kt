package info.papdt.express.helper.model

class BaseMessage<T> {

    var code: Int = 0
    var data: T? = null

    constructor(code: Int, `object`: T) {
        this.code = code
        this.data = `object`
    }

    constructor(code: Int) {
        this.code = code
    }

    constructor()

    companion object {

        const val CODE_OKAY = 200
        const val CODE_ERROR = -1

    }

}
