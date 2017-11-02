package info.papdt.express.helper.api

import android.util.Log
import com.google.firebase.iid.FirebaseInstanceId
import com.google.gson.Gson
import info.papdt.express.helper.model.ResponseMessage
import info.papdt.express.helper.support.SettingsInstance
import info.papdt.express.helper.support.postForm
import info.papdt.express.helper.support.postJson
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
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

	@JvmOverloads fun register(token: String = defaultToken): Observable<ResponseMessage>
			= Observable.just(token).map { targetToken ->
				if (apiHost.isEmpty()) return@map ResponseMessage()
				val request = Request.Builder()
						.postForm(mapOf("token" to targetToken))
						.url("$apiHost/subscribe/register")
						.build()
				return@map requestJsonObject<ResponseMessage>(request) ?: ResponseMessage()
			}
			.subscribeOn(Schedulers.io())
			.observeOn(AndroidSchedulers.mainThread())

	@JvmOverloads fun sync(list: Collection<String>, token: String = defaultToken): Observable<ResponseMessage> {
		return Observable.just(list)
				.map { syncList ->
					if (apiHost.isEmpty()) return@map ResponseMessage()
					val strings = syncList.map { "\"$it\"" }
					val dataString = if (strings.isEmpty()) "" else strings.reduce { acc, s -> "$acc,$s" }
					val request = Request.Builder()
							.postJson("[$dataString]")
							.url("$apiHost/subscribe/sync?token=$token")
							.build()
					return@map requestJsonObject<ResponseMessage>(request) ?: ResponseMessage()
				}
				.subscribeOn(Schedulers.io())
				.observeOn(AndroidSchedulers.mainThread())
	}

	@JvmOverloads fun add(number: String, company: String? = null, token: String = defaultToken): Observable<ResponseMessage> {
		return Observable.just("")
				.map {
					if (apiHost.isEmpty()) return@map ResponseMessage()
					val request = Request.Builder()
							.postForm(mutableMapOf("token" to token, "id" to number).apply {
								company?.let { this["com"] = it }
							})
							.url("$apiHost/subscribe/add")
							.build()
					return@map requestJsonObject<ResponseMessage>(request) ?: ResponseMessage()
				}
				.subscribeOn(Schedulers.io())
				.observeOn(AndroidSchedulers.mainThread())
	}

	@JvmOverloads fun remove(number: String, token: String = defaultToken): Observable<ResponseMessage> {
		return Observable.just(number)
				.map { id ->
					if (apiHost.isEmpty()) return@map ResponseMessage()
					val request = Request.Builder()
							.postForm(mapOf("token" to token, "id" to id))
							.url("$apiHost/subscribe/remove")
							.build()
					return@map requestJsonObject<ResponseMessage>(request) ?: ResponseMessage()
				}
				.subscribeOn(Schedulers.io())
				.observeOn(AndroidSchedulers.mainThread())
	}

	@JvmOverloads fun list(token: String = defaultToken): Observable<Array<String>?> {
		return Observable.just("")
				.map {
					if (apiHost.isEmpty()) return@map emptyArray<String>()
					val request = Request.Builder()
							.url("$apiHost/subscribe/list?token=$token")
							.build()
					return@map requestJsonObject<Array<String>>(request)
				}
				.subscribeOn(Schedulers.io())
				.observeOn(AndroidSchedulers.mainThread())
	}

	@JvmOverloads fun requestPush(token: String = defaultToken): Observable<ResponseMessage> {
		return Observable.just("")
				.map {
					if (apiHost.isEmpty()) return@map ResponseMessage()
					val request = Request.Builder()
							.url("$apiHost/subscribe/request_push?token=$token")
							.build()
					return@map requestJsonObject<ResponseMessage>(request) ?: ResponseMessage()
				}
				.subscribeOn(Schedulers.io())
				.observeOn(AndroidSchedulers.mainThread())
	}

}