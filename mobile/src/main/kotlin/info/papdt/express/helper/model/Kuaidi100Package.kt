package info.papdt.express.helper.model

import com.google.gson.Gson
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

import java.util.ArrayList
import java.util.regex.Pattern

import info.papdt.express.helper.api.Kuaidi100PackageApi

class Kuaidi100Package {

    /** Query data  */
    @Expose @SerializedName("message") var message: String? = null
    @Expose @SerializedName("nu") var number: String? = null
    @Expose @SerializedName("com") var companyType: String? = null
    @Expose @SerializedName("companytype") private val companyType1: String? = null
    @Expose @SerializedName("ischeck") var isCheck: String? = null
    @Expose @SerializedName("updatetime") var updateTime: String? = null
    @Expose @SerializedName("status") var status: String? = null
    @Expose @SerializedName("condition") var condition: String? = null
    @Expose @SerializedName("codenumber") var codeNumber: String? = null
    @Expose @SerializedName("data") var data: ArrayList<Status>? = null
    @Expose @SerializedName("state") private var state: String? = null

    /** Local data  */
    @Expose var shouldPush = false
    @Expose var unreadNew = false
    @Expose var name: String? = null
    @Expose var companyChineseName: String? = null

    val id: Long
        get() {
            var l: Long = 0
            for (i in 0 until number!!.length) {
                val s: String = number!!.substring(i, i + 1)
                if (s[0].isDigit()) {
                    l = l * 10 + s.toLong()
                }
            }
            return l
        }

    fun getState(): Int {
        return if (state != null) Integer.parseInt(state) else STATUS_FAILED
    }

    fun setState(status: Int) {
        this.state = status.toString()
    }

    fun applyNewData(newData: Kuaidi100Package?) {
        if (newData == null) return

        try {
            this.shouldPush = !this.data!![0].time!!.equals(newData.data!![0].time!!, ignoreCase = true)
            this.unreadNew = this.unreadNew or this.shouldPush
        } catch (e: Exception) {
            if (newData.data != null && this.data == null) {
                this.shouldPush = true
                this.unreadNew = this.unreadNew or this.shouldPush
            } else {
                this.shouldPush = false
                this.unreadNew = this.unreadNew or this.shouldPush
            }
        }

        this.status = newData.status
        this.state = newData.state
        this.updateTime = newData.updateTime
        this.isCheck = newData.isCheck
        this.condition = newData.condition
        this.message = newData.message
        if (newData.data != null && !newData.data!!.isEmpty()) {
            this.data = newData.data
        } else {
            this.shouldPush = false
            this.unreadNew = this.shouldPush
        }
    }

    fun toJsonString(): String {
        return Gson().toJson(this)
    }

    class Status {

        @Expose var time: String? = null
        @Expose @SerializedName("location") var _location: String? = null
        @Expose var context: String? = null
        @Expose var ftime: String? = null
        @Expose @SerializedName("phone") var _phone: String? = null

        private fun processOldData() {
            val qszp = "签收照片,"
            if (context?.startsWith(qszp) == true) {
                context = context!!.substring(
                        context!!.indexOf(qszp) + qszp.length, context!!.length)
                        .trim { it <= ' ' }
            }
        }

        fun getLocation(): String? {
            processOldData() // dirty method
            if (_location != null) {
                _location = _location!!.replace("【", "").replace("】", "").replace("[", "").replace("]", "")
                if (_location!!.trim { it <= ' ' }.isNotEmpty()) {
                    if (!_location!!.trim { it <= ' ' }.contains("null")) {
                        return _location
                    }
                }
            }

            if (context?.contains("【") == true) {
                _location = context!!.substring(context!!.indexOf("【") + 1, context!!.indexOf("】")).trim { it <= ' ' }
            }
            if (context?.contains("[") == true) {
                _location = context!!.substring(context!!.indexOf("[") + 1, context!!.indexOf("]")).trim { it <= ' ' }
            }
            if (_location != null) {
                if (_location!!.trim { it <= ' ' }.contains("null")) {
                    _location = null
                } else {
                    _location = _location!!.replace("【", "").replace("】", "").replace("[", "").replace("]", "")
                }
            }
            return _location
        }

        fun getPhone(): String? {
            return _phone ?: Status.findContact(context).apply { _phone = this }
        }

        companion object {

            fun findContact(s: String?): String? {
                var number: String? = checkNum(s)
                if (number == null || number.length < 8) return null
                if (number.contains(",")) number = number.substring(0, number.indexOf(","))
                return number
            }

            private fun checkNum(num: String?): String {
                if (num?.isEmpty() != false) return ""
                val pattern = Pattern.compile("(?<!\\d)(?:(?:1[3578]\\d{9})|(?:861[3578]\\d{9}))(?!\\d)")
                val matcher = pattern.matcher(num)
                val bf = StringBuffer(64)
                while (matcher.find()) {
                    bf.append(matcher.group()).append(",")
                }
                val len = bf.length
                if (len > 0) {
                    bf.deleteCharAt(len - 1)
                }
                return bf.toString()
            }
        }

    }

    companion object {

        const val STATUS_FAILED = 2
        const val STATUS_NORMAL = 0
        const val STATUS_ON_THE_WAY = 5
        const  val STATUS_DELIVERED = 3
        const val STATUS_RETURNED = 4
        const val STATUS_RETURNING = 6
        const val STATUS_OTHER = 1

        @JvmStatic fun buildFromJson(json: String): Kuaidi100Package {
            try {
                val p = Gson().fromJson(json, Kuaidi100Package::class.java)
                if (p.companyChineseName == null && p.companyType != null) {
                    p.companyChineseName = Kuaidi100PackageApi.CompanyInfo.getNameByCode(p.companyType!!)
                }
                if (p.companyType == null) {
                    p.companyType = p.companyType1
                }
                return p
            } catch (e: Exception) {
                //may not be a json string
                e.printStackTrace()
                return Kuaidi100Package()
            }
        }
    }

}
