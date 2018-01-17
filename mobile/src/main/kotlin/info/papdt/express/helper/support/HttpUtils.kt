package info.papdt.express.helper.support

import android.util.Log

import java.io.IOException
import java.util.concurrent.TimeUnit

import info.papdt.express.helper.model.BaseMessage
import okhttp3.OkHttpClient
import okhttp3.Request

object HttpUtils {

    private val client = OkHttpClient.Builder()
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(10, TimeUnit.SECONDS)
            .build()

    private const val UA_CHROME = "Mozilla/5.0 (Windows NT 6.1; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/51.0.2704.84 Safari/537.36"

    private val TAG = HttpUtils::class.java.simpleName

    fun getString(url: String, ua: String): BaseMessage<String> {
        val result = BaseMessage<String>()

        val request = Request.Builder().url(url).addHeader("User-Agent", ua).build()
        try {
            val response = client.newCall(request).execute()
            result.code = response.code()
            result.data = response.body()!!.string()
            Log.i(TAG, result.data)
        } catch (e: IOException) {
            result.code = BaseMessage.CODE_ERROR
            e.printStackTrace()
        }

        return result
    }

    fun getString(url: String, useLocalUA: Boolean): BaseMessage<String>
            = getString(url, if (useLocalUA) System.getProperty("http.agent") else UA_CHROME)

}
