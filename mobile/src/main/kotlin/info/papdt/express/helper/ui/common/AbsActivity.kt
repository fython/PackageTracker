package info.papdt.express.helper.ui.common

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

abstract class AbsActivity : AppCompatActivity() {

	@JvmField protected var mToolbar: Toolbar? = null
	@JvmField protected var mActionBar: ActionBar? = null
	protected val settings: Settings by lazy { Settings.getInstance(applicationContext) }

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

	protected fun <T : View> `$`(viewId: Int): T? {
		return findViewById<View>(viewId) as T
	}

	override fun onOptionsItemSelected(item: MenuItem): Boolean {
		if (item.itemId == android.R.id.home) {
			this.onBackPressed()
			return true
		}
		return super.onOptionsItemSelected(item)
	}

}
