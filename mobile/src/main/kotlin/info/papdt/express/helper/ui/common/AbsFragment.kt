package info.papdt.express.helper.ui.common

import android.content.Context
import android.os.Bundle
import androidx.annotation.IdRes
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import info.papdt.express.helper.support.Settings
import kotlinx.coroutines.*
import kotlin.coroutines.CoroutineContext

abstract class AbsFragment : Fragment(), CoroutineScope {

	protected var rootView: View? = null
	protected lateinit var settings: Settings

	protected abstract fun getLayoutResId(): Int

	private lateinit var job: Job
	override val coroutineContext: CoroutineContext get() = Dispatchers.Main + job

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		job = Job()

		settings = Settings.getInstance(context!!)
	}

    override fun onDestroy() {
        super.onDestroy()
        job.cancel()
    }

	override fun onCreateView(inflater: LayoutInflater, parent: ViewGroup?, state: Bundle?): View? {
		if (parent != null) {
			rootView = inflater.inflate(getLayoutResId(), parent, false)
		} else {
			rootView = inflater.inflate(getLayoutResId(), null)
		}
		return rootView
	}

    fun ui(block: suspend CoroutineScope.() -> Unit) {
        launch(coroutineContext, block = {
            try {
                block()
            } catch (e: Exception) {
                e.printStackTrace()
                throw e
            }
        })
    }

    fun <T> asyncIO(block: suspend CoroutineScope.() -> T): Deferred<T> {
        return async(Dispatchers.IO, block = {
            try {
                block()
            } catch (e: Exception) {
                e.printStackTrace()
                throw e
            }
        })
    }

}
