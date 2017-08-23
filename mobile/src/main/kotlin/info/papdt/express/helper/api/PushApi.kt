package info.papdt.express.helper.api

import android.util.Log
import com.google.gson.Gson
import info.papdt.express.helper.model.ResponseMessage
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

	var apiHost = "192.168.1.101"

	fun register(token: String): Observable<ResponseMessage> = Observable.just(token)
			.map { targetToken ->
				var result: ResponseMessage
				val request = Request.Builder()
						.method("POST",
								RequestBody.create(
										MediaType.parse("application/x-www-form-urlencoded"),
										"token=$targetToken"
								)
						)
						.url("http://$apiHost/register")
						.build()
				try {
					val response = client.newCall(request).execute()
					val string = response.body().string()
					result = Gson().fromJson(string, ResponseMessage::class.java)
					Log.i(TAG, string)
				} catch (e: IOException) {
					result = ResponseMessage()
					e.printStackTrace()
				}
				return@map result
			}
			.subscribeOn(Schedulers.io())
			.observeOn(AndroidSchedulers.mainThread())

}