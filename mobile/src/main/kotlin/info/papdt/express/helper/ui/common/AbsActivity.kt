package info.papdt.express.helper.ui.common

import android.content.BroadcastReceiver
import android.content.IntentFilter
import android.content.res.Configuration
import android.os.Bundle
import android.support.annotation.LayoutRes
import android.support.v7.app.ActionBar
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.view.MenuItem
import android.view.View

import info.papdt.express.helper.R
import info.papdt.express.helper.support.Settings
import info.papdt.express.helper.support.isFontProviderEnabled
import info.papdt.express.helper.support.localBroadcastManager
import moe.shizuku.fontprovider.FontProviderClient

abstract class AbsActivity : AppCompatActivity() {

	@JvmField protected var mToolbar: Toolbar? = null
	@JvmField protected var mActionBar: ActionBar? = null
	protected val settings: Settings by lazy { Settings.getInstance(applicationContext) }

    private val localBroadcastReceivers: MutableList<BroadcastReceiver> = mutableListOf()

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)

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
