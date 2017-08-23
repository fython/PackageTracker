package info.papdt.express.helper.services

import android.util.Log
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.google.gson.Gson

class FCMService : FirebaseMessagingService() {

	override fun onMessageReceived(remoteMessage: RemoteMessage?) {
		Log.i("TAG", Gson().toJson(remoteMessage?.data))
	}

}
