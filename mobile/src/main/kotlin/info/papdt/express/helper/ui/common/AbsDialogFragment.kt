package info.papdt.express.helper.ui.common

import android.os.Bundle
import androidx.fragment.app.DialogFragment
import kotlinx.coroutines.*
import kotlin.coroutines.CoroutineContext

abstract class AbsDialogFragment : DialogFragment(), CoroutineScope {

    private lateinit var job: Job
    override val coroutineContext: CoroutineContext get() = Dispatchers.Main + job

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        job = Job()
    }

    override fun onDestroy() {
        super.onDestroy()
        job.cancel()
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