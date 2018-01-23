package info.papdt.express.helper.model

import com.google.gson.annotations.SerializedName
import java.text.SimpleDateFormat
import java.util.*

class BaiduPackage {

    @SerializedName("msg") var message: String = ""
    @SerializedName("status") private var _status: String = "-1"
    @SerializedName("error_code") private var _errorCode: String = "-1"

    var status: Int
        get() = _status.toIntOrNull() ?: -1
        set(value) { _status = value.toString() }
    var errorCode: Int
        get() = _errorCode.toIntOrNull() ?: -1
        set(value) { _errorCode = value.toString() }

    var number: String = ""
    var data: Data = Data()

    fun toKuaidi100PackageType(): Kuaidi100Package {
        val result = Kuaidi100Package()
        result.message = message
        result.number = number
        result.codeNumber = number
        result.companyType = data.com
        result.companyChineseName = data.company["fullname"] as String
        result.status = if (data.info.status == INFO_STATUS_SUCCESS) "200" else "400"
        result.setState(data.info.state)
        result.data = ArrayList(data.info.context.map(Data.Info.Status::toKuaidi100Status))
        return result
    }

    class Data {

        var info: Info = Info()
        var com: String = ""
        var company: Map<String, Any> = mapOf()

        class Info {

            @SerializedName("status") private var _status: String = ""
            @SerializedName("state") private var _state: String = ""
            var com: String = ""
            var context: List<Status> = listOf()
            @SerializedName("_support_from") var supportFrom: String? = null

            var status: Int
                get() = _status.toIntOrNull() ?: INFO_STATUS_FAILED
                set(value) { _status = value.toString() }
            var state: Int
                get() = _state.toIntOrNull() ?: STATE_FAILED
                set(value) { _state = value.toString() }

            class Status {

                @SerializedName("time") private var _time = "0"
                val time: Int get() = _time.toIntOrNull() ?: 0
                @SerializedName("desc") var text = ""

                fun toKuaidi100Status(): Kuaidi100Package.Status {
                    val status = Kuaidi100Package.Status()
                    status.context = text
                    status.time = Date(time * 1000L).let {
                        SimpleDateFormat(
                                "yyyy-MM-dd HH:mm:ss",
                                Locale.getDefault()
                        ).format(it)
                    }
                    status.ftime = status.time
                    return status
                }

            }

        }

    }

    companion object {

        const val INFO_STATUS_NO_RESULT = 0
        const val INFO_STATUS_SUCCESS = 1
        const val INFO_STATUS_FAILED = 2

        const val STATE_NORMAL = 0
        const val STATE_COLLECTING = 1
        const val STATE_FAILED = 2
        const val STATE_DELIVERED = 3
        const val STATE_RETURNED = 4
        const val STATE_ON_THE_WAY = 5
        const val STATE_RETURNING = 6

    }

}