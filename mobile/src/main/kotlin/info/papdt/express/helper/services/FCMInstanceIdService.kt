package info.papdt.express.helper.services

import android.util.Log
import com.google.firebase.iid.FirebaseInstanceId
import com.google.firebase.iid.FirebaseInstanceIdService
import info.papdt.express.helper.api.PushApi
import info.papdt.express.helper.support.Settings

class FCMInstanceIdService : FirebaseInstanceIdService() {

	override fun onTokenRefresh() {
		// Get updated InstanceID token.
		val refreshedToken = FirebaseInstanceId.getInstance().token
		Log.d(FCMInstanceIdService::class.java.simpleName, "Refreshed token: " + refreshedToken!!)

		Settings.getInstance(this).putString(Settings.KEY_FIREBASE_INSTANCE_ID, refreshedToken)
		PushApi.register(refreshedToken).subscribe()
	}

}
