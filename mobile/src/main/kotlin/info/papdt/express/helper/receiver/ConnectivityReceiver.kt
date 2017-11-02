package info.papdt.express.helper.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

import info.papdt.express.helper.support.PushUtils
import moe.feng.kotlinyan.common.*

class ConnectivityReceiver : BroadcastReceiver() {

	override fun onReceive(context: Context, intent: Intent) {
		context.run(if (readNetworkState(context)) PushUtils::startServices else PushUtils::stopServices)
	}

	companion object {

		fun readNetworkState(context: Context?) = context?.connectivityManager?.activeNetworkInfo?.isConnected ?: false

	}

}