package info.papdt.express.helper.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.localbroadcastmanager.content.LocalBroadcastManager

abstract class ActionBroadcastReceiver(val intentFilter: IntentFilter) : BroadcastReceiver() {

    companion object {

        inline fun create(intentFilter: IntentFilter,
                   crossinline block: (context: Context, intent: Intent?) -> Unit)
                : ActionBroadcastReceiver {
            return object : ActionBroadcastReceiver(intentFilter) {
                override fun onReceive(context: Context, intent: Intent?) {
                    block(context, intent)
                }
            }
        }

        inline fun create(action: String,
                          crossinline block: (context: Context, intent: Intent?) -> Unit)
                : ActionBroadcastReceiver {
            return create(IntentFilter(action), block)
        }

    }

    fun registerLocal(localBroadcastManager: LocalBroadcastManager) {
        localBroadcastManager.registerReceiver(this, intentFilter)
    }

    fun unregisterLocal(localBroadcastManager: LocalBroadcastManager) {
        localBroadcastManager.unregisterReceiver(this)
    }

    fun register(context: Context) {
        context.registerReceiver(this, intentFilter)
    }

    fun unregister(context: Context) {
        context.unregisterReceiver(this)
    }

}