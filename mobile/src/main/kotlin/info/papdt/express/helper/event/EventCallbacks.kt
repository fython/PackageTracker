package info.papdt.express.helper.event

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import info.papdt.express.helper.EXTRA_DATA
import info.papdt.express.helper.model.Kuaidi100Package
import moe.feng.kotlinyan.common.get

object EventCallbacks {

    private const val TAG = "EventCallbacks"

    fun deletePackage(callback: (data: Kuaidi100Package) -> Unit): BroadcastReceiver {
        return object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent?) {
                if (intent == null || !intent.hasExtra(EXTRA_DATA)) {
                    Log.e(TAG, "deletePackage: receive empty data. It is invalid.")
                    return
                }
                callback(intent[EXTRA_DATA]!!.asParcelable())
            }
        }
    }

}