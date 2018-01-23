package info.papdt.express.helper.api

import android.net.Uri
import com.google.gson.Gson
import info.papdt.express.helper.model.BaiduPackage
import info.papdt.express.helper.model.BaseMessage
import info.papdt.express.helper.model.Kuaidi100Package
import info.papdt.express.helper.support.HttpUtils
import okhttp3.Request
import okhttp3.Response
import okhttp3.ResponseBody
import java.io.IOException

object BaiduPackageApi {

    private const val API_URL = "https://sp0.baidu.com/9_Q4sjW91Qh3otqbppnN2DJv/pae" +
            "/channel/data/asyncqury?cb&appid=4001&nu=123456789012&vcode&token&_=1516636621"

    private var shouldRequestBaidu = true

    private fun getQueryUrl(number: String): String {
        return Uri.parse(API_URL).buildUpon().apply {
            appendQueryParameter("cb", "")
            appendQueryParameter("appid", "4001")
            appendQueryParameter("nu", number)
            appendQueryParameter("vcode", "")
            appendQueryParameter("token", "")
            appendQueryParameter("_", (System.currentTimeMillis() / 1000).toString())
        }.build().toString()
    }

    @JvmStatic fun getPackageByNumber(number: String): BaseMessage<out Kuaidi100Package?> {
        try {
            if (shouldRequestBaidu) {
                initBaiduCookies() ?: throw IOException("Cannot access cookie.")
                shouldRequestBaidu = false
            }
            val string = Request.Builder()
                    .url(getQueryUrl(number))
                    .addHeader(HttpUtils.HEADER_UA, HttpUtils.UA_CHROME)
                    .build()
                    .let(HttpUtils.client::newCall)
                    .execute()
                    .let(Response::body)!!
                    .let(ResponseBody::string)
            val baiduModel = Gson().fromJson(string, BaiduPackage::class.java)
            baiduModel.number = number
            return BaseMessage(BaseMessage.CODE_OKAY, baiduModel.toKuaidi100PackageType())
        } catch (e : Exception) {
            e.printStackTrace()
            return BaseMessage(BaseMessage.CODE_ERROR)
        }
    }

    private fun initBaiduCookies(): String? {
        return Request.Builder()
                .url("https://www.baidu.com")
                .addHeader(HttpUtils.HEADER_UA, HttpUtils.UA_CHROME)
                .build()
                .let(HttpUtils.client::newCall)
                .execute()
                .let { response ->
                    try {
                        response.body()?.string()
                    } catch (e : Exception) {
                        null
                    }
                }
    }

}