package info.papdt.express.helper.event

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Parcelable
import android.util.Log
import androidx.recyclerview.widget.RecyclerView
import info.papdt.express.helper.EXTRA_DATA
import info.papdt.express.helper.EXTRA_OLD_DATA
import info.papdt.express.helper.model.Category
import info.papdt.express.helper.model.Kuaidi100Package
import info.papdt.express.helper.receiver.ActionBroadcastReceiver
import info.papdt.express.helper.receiver.ActionBroadcastReceiver.Companion.create
import me.drakeet.multitype.ItemViewBinder
import moe.feng.kotlinyan.common.get
import kotlin.reflect.KClass

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

    fun onDeleteCategory(callback: (data: Category) -> Unit): BroadcastReceiver {
        return object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent?) {
                if (intent == null || !intent.hasExtra(EXTRA_DATA)) {
                    Log.e(TAG, "onDeleteCategory: receive empty data")
                    return
                }
                callback(intent[EXTRA_DATA]!!.asParcelable())
            }
        }
    }

    fun onSaveNewCategory(callback: (data: Category) -> Unit): BroadcastReceiver {
        return object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent?) {
                if (intent == null || !intent.hasExtra(EXTRA_DATA)) {
                    Log.e(TAG, "onSaveNewCategory: receive empty data")
                    return
                }
                callback(intent[EXTRA_DATA]!!.asParcelable())
            }
        }
    }

    fun onSaveEditCategory(
            callback: (oldData: Category, data: Category) -> Unit): BroadcastReceiver {
        return object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent?) {
                if (intent == null
                        || !intent.hasExtra(EXTRA_DATA)
                        || !intent.hasExtra(EXTRA_OLD_DATA)) {
                    Log.e(TAG, "onSaveEditCategory: receive empty data")
                    return
                }
                callback(
                        intent[EXTRA_OLD_DATA]!!.asParcelable(),
                        intent[EXTRA_DATA]!!.asParcelable()
                )
            }
        }
    }

    fun <T : Parcelable, VH : RecyclerView.ViewHolder, B: ItemViewBinder<T, VH>> onItemClick(
            clazz: KClass<B>,
            callback: (data: T?) -> Unit): BroadcastReceiver {
        return create(EventIntents.getItemOnClickActionName(clazz)) { _, intent ->
            if (intent == null || !intent.hasExtra(EXTRA_DATA)) {
                Log.e(TAG, "onItemClick: receive empty data")
                return@create
            }
            callback(intent[EXTRA_DATA]?.asParcelable())
        }
    }

}