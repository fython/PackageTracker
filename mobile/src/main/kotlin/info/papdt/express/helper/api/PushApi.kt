package info.papdt.express.helper.api

import android.util.Log
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

	private val apiHost: String get() = SettingsInstance.run { "$pushApiHost:$pushApiPort" }
	private var token: String = "null"

	private inline fun <reified T> requestJsonObject(request: Request): T? {
		try {
			val response = client.newCall(request).execute()
			val string = response.body().string()
			Log.i(TAG, string)
			return Gson().fromJson(string, T::class.java)
		} catch (e: IOException) {
			e.printStackTrace()
		} catch (e: Exception) {
			e.printStackTrace()
		}
		return null
	}

	fun register(token: String): Observable<ResponseMessage> = Observable.just(token)
			.map { targetToken ->
				PushApi.token = targetToken
				val request = Request.Builder()
						.postForm(mapOf("token" to targetToken))
						.url("http://$apiHost/subscribe/register")
						.build()
				return@map requestJsonObject<ResponseMessage>(request) ?: ResponseMessage()
			}
			.subscribeOn(Schedulers.io())
			.observeOn(AndroidSchedulers.mainThread())

	fun sync(list: Collection<String>, token: String? = null): Observable<ResponseMessage> {
		token?.let { PushApi.token = it }
		return Observable.just(list)
				.map { syncList ->
					val postBody = "[${syncList.map { "\"$it\"" }.reduce { acc, s -> "$acc,$s" }}]"
					Log.i(TAG, "Post body: $postBody")
					val request = Request.Builder()
							.postJson(postBody)
							.url("http://$apiHost/subscribe/sync?token=${PushApi.token}")
							.build()
					return@map requestJsonObject<ResponseMessage>(request) ?: ResponseMessage()
				}
				.subscribeOn(Schedulers.io())
				.observeOn(AndroidSchedulers.mainThread())
	}

	fun add(number: String, company: String? = null, token: String? = null): Observable<ResponseMessage> {
		token?.let { PushApi.token = it }
		return Observable.just("")
				.map {
					val request = Request.Builder()
							.postForm(mutableMapOf("token" to PushApi.token, "id" to number).apply {
								company?.let { this["com"] = it }
							})
							.url("http://$apiHost/subscribe/add")
							.build()
					return@map requestJsonObject<ResponseMessage>(request) ?: ResponseMessage()
				}
	}

	fun remove(number: String, token: String? = null): Observable<ResponseMessage> {
		token?.let { PushApi.token = it }
		return Observable.just(number)
				.map { id ->
					val request = Request.Builder()
							.postForm(mapOf("token" to PushApi.token, "id" to id))
							.url("http://$apiHost/subscribe/remove")
							.build()
					return@map requestJsonObject<ResponseMessage>(request) ?: ResponseMessage()
				}
	}

	fun list(token: String? = null): Observable<Array<String>?> {
		token?.let { PushApi.token = it }
		return Observable.just("")
				.map {
					val request = Request.Builder()
							.url("http://$apiHost/subscribe/list?token=${PushApi.token}")
							.build()
					return@map requestJsonObject<Array<String>>(request)
				}
	}

}