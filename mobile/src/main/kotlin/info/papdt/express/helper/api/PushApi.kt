package info.papdt.express.helper.api

import android.util.Log
import com.google.firebase.iid.FirebaseInstanceId
import com.google.gson.Gson
import info.papdt.express.helper.model.ResponseMessage
import info.papdt.express.helper.support.SettingsInstance
import info.papdt.express.helper.support.postForm
import info.papdt.express.helper.support.postJson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.*

import java.io.IOException
import java.util.concurrent.TimeUnit

object PushApi {

	private val TAG = PushApi::class.java.simpleName

	private val client = OkHttpClient.Builder()
			.connectTimeout(10, TimeUnit.SECONDS)
			.readTimeout(10, TimeUnit.SECONDS)
			.build()

	private val apiHost: String get() = SettingsInstance.run {
		"http${if (enableHttps) "s" else ""}://$pushApiHost:$pushApiPort"
	}
	private val defaultToken: String get() = FirebaseInstanceId.getInstance().token ?: "null"

	private inline fun <reified T> requestJsonObject(request: Request): T? {
		try {
			val response = client.newCall(request).execute()
			val string = response.body()!!.string()
			Log.i(TAG, string)
			return Gson().fromJson(string, T::class.java)
		} catch (e: IOException) {
			e.printStackTrace()
		} catch (e: Exception) {
			e.printStackTrace()
		}
		return null
	}

	@JvmOverloads
	suspend fun register(
			token: String = defaultToken
	): ResponseMessage = withContext(Dispatchers.IO) {
        if (SettingsInstance.pushApiHost == null) {
            return@withContext ResponseMessage()
        }
		if (apiHost.isEmpty()) return@withContext ResponseMessage()
		val request = Request.Builder()
				.postForm(mapOf("token" to token))
				.url("$apiHost/subscribe/register")
				.build()
		return@withContext requestJsonObject<ResponseMessage>(request) ?: ResponseMessage()
    }

	@JvmOverloads
	suspend fun sync(
			list: Collection<String>,
			token: String = defaultToken
	): ResponseMessage = withContext(Dispatchers.IO) {
        if (SettingsInstance.pushApiHost == null) {
            return@withContext ResponseMessage()
        }
		if (apiHost.isEmpty()) return@withContext ResponseMessage()
		val strings = list.map { "\"$it\"" }
		val dataString = if (strings.isEmpty()) "" else strings.reduce { acc, s -> "$acc,$s" }
		val request = Request.Builder()
				.postJson("[$dataString]")
				.url("$apiHost/subscribe/sync?token=$token")
				.build()
		return@withContext requestJsonObject<ResponseMessage>(request) ?: ResponseMessage()
	}

	@JvmOverloads
	suspend fun add(
			number: String,
			company: String? = null,
			token: String = defaultToken
	): ResponseMessage = withContext(Dispatchers.IO) {
        if (SettingsInstance.pushApiHost == null) {
            return@withContext ResponseMessage()
        }
		if (apiHost.isEmpty()) return@withContext ResponseMessage()
		val request = Request.Builder()
				.postForm(mutableMapOf("token" to token, "id" to number).apply {
					company?.let { this["com"] = it }
				})
				.url("$apiHost/subscribe/add")
				.build()
		return@withContext requestJsonObject<ResponseMessage>(request) ?: ResponseMessage()
	}

	@JvmOverloads
	suspend fun remove(
			number: String,
			token: String = defaultToken
	): ResponseMessage = withContext(Dispatchers.IO) {
        if (SettingsInstance.pushApiHost == null) {
            return@withContext ResponseMessage()
        }
		if (apiHost.isEmpty()) return@withContext ResponseMessage()
		val request = Request.Builder()
				.postForm(mapOf("token" to token, "id" to number))
				.url("$apiHost/subscribe/remove")
				.build()
		return@withContext requestJsonObject<ResponseMessage>(request) ?: ResponseMessage()
	}

	@JvmOverloads
    suspend fun list(token: String = defaultToken): Array<String> = withContext(Dispatchers.IO) {
        if (SettingsInstance.pushApiHost == null) {
            return@withContext emptyArray<String>()
        }
        if (apiHost.isEmpty()) return@withContext emptyArray<String>()
        val request = Request.Builder()
                .url("$apiHost/subscribe/list?token=$token")
                .build()
        return@withContext requestJsonObject<Array<String>>(request) ?: emptyArray()
	}

	@JvmOverloads
    suspend fun requestPush(
            token: String = defaultToken
    ): ResponseMessage = withContext(Dispatchers.IO) {
		if (SettingsInstance.pushApiHost == null) {
            return@withContext ResponseMessage()
        }
        if (SettingsInstance.pushApiHost.isNullOrEmpty()) return@withContext ResponseMessage()
        val request = Request.Builder()
                .url("$apiHost/subscribe/request_push?token=$token")
                .build()
        return@withContext requestJsonObject<ResponseMessage>(request) ?: ResponseMessage()
	}

}