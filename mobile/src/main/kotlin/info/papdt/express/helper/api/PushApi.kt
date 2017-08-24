package info.papdt.express.helper.api

import android.util.Log
import com.google.gson.Gson
import info.papdt.express.helper.model.ResponseMessage
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import okhttp3.*

import java.io.IOException
import java.net.SocketTimeoutException
import java.util.concurrent.TimeUnit

object PushApi {

	private val TAG = PushApi::class.java.simpleName

	private val client = OkHttpClient.Builder()
			.connectTimeout(10, TimeUnit.SECONDS)
			.readTimeout(10, TimeUnit.SECONDS)
			.build()

	var apiHost = "192.168.1.105:3000"
	private var token: String = "null"

	private inline fun <reified T> requestJsonObject(request: Request): T? {
		try {
			val response = client.newCall(request).execute()
			val string = response.body().string()
			Log.i(TAG, string)
			return Gson().fromJson(string, T::class.java)
		} catch (e: IOException) {
			e.printStackTrace()
		} catch (e: SocketTimeoutException) {
		} catch (e: Exception) {
			e.printStackTrace()
		}
		return null
	}

	fun register(token: String): Observable<ResponseMessage> = Observable.just(token)
			.map { targetToken ->
				PushApi.token = targetToken
				val request = Request.Builder()
						.method("POST",
								RequestBody.create(
										MediaType.parse("application/x-www-form-urlencoded"),
										"token=$targetToken"
								)
						)
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
							.method("POST",
									RequestBody.create(MediaType.parse("application/json"), postBody)
							)
							.url("http://$apiHost/subscribe/sync?token=${PushApi.token}")
							.build()
					return@map requestJsonObject<ResponseMessage>(request) ?: ResponseMessage()
				}
				.subscribeOn(Schedulers.io())
				.observeOn(AndroidSchedulers.mainThread())
	}

}