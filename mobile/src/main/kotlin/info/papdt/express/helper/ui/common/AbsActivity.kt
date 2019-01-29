package info.papdt.express.helper.ui.common

import android.content.BroadcastReceiver
import android.content.IntentFilter
import android.content.res.Configuration
import android.os.Bundle
import androidx.annotation.LayoutRes
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import android.view.MenuItem

import info.papdt.express.helper.R
import info.papdt.express.helper.support.Settings
import info.papdt.express.helper.support.isFontProviderEnabled
import info.papdt.express.helper.support.localBroadcastManager
import kotlinx.coroutines.*
import moe.shizuku.fontprovider.FontProviderClient
import kotlin.coroutines.CoroutineContext

abstract class AbsActivity : AppCompatActivity(), CoroutineScope {

	@JvmField protected var mToolbar: Toolbar? = null
	@JvmField protected var mActionBar: ActionBar? = null
	protected val settings: Settings by lazy { Settings.getInstance(applicationContext) }

    private val localBroadcastReceivers: MutableList<BroadcastReceiver> = mutableListOf()

    private lateinit var job: Job
    override val coroutineContext: CoroutineContext get() = Dispatchers.Main + job

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
        job = Job()

		// Load Noto Sans CJK from FontProvider
		FontProviderClient.create(this)?.let { client ->
			client.setNextRequestReplaceFallbackFonts(true)
			val results = client.replace(
					"Noto Sans CJK",
					"sans-serif", "sans-serif-medium")
			if (results?.isNotEmpty() == true) {
				isFontProviderEnabled = true
			}
		}
	}

    override fun onDestroy() {
        super.onDestroy()
        job.cancel()
    }

	override fun setContentView(@LayoutRes layoutResId: Int) {
		super.setContentView(layoutResId)
		mToolbar = findViewById(R.id.toolbar)
		if (mToolbar != null) {
			setSupportActionBar(mToolbar)
			mActionBar = supportActionBar
		}
		setUpViews()
	}

	val nightMode: Int
		get() = resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK

	val isNightMode: Boolean
		get() = nightMode == Configuration.UI_MODE_NIGHT_YES

	protected abstract fun setUpViews()

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

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
		if (item.itemId == android.R.id.home) {
			this.onBackPressed()
			return true
		}
		return super.onOptionsItemSelected(item)
	}

    override fun onStop() {
        super.onStop()
        unregisterAllLocalBroadcastReceiver()
    }

    fun registerLocalBroadcastReceiver(broadcastReceiver: BroadcastReceiver,
                                       actionFilter: IntentFilter? = null,
                                       action: String? = null) {
        if (actionFilter == null && action == null) {
            throw IllegalArgumentException("Please set a action in actionFilter/action arguments.")
        }
        localBroadcastReceivers += broadcastReceiver
        localBroadcastManager.registerReceiver(
                broadcastReceiver, actionFilter ?: IntentFilter(action))
    }

    fun unregisterLocalBroadcastReceiver(broadcastReceiver: BroadcastReceiver) {
        localBroadcastReceivers -= broadcastReceiver
        localBroadcastManager.unregisterReceiver(broadcastReceiver)
    }

    fun unregisterAllLocalBroadcastReceiver() {
        for (item in localBroadcastReceivers) {
            localBroadcastManager.unregisterReceiver(item)
        }
        localBroadcastReceivers.clear()
    }

}
