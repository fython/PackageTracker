package info.papdt.express.helper.ui

import android.app.Activity
import android.app.FragmentTransaction
import android.content.Intent
import android.graphics.Color
import android.os.AsyncTask
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.support.annotation.IdRes
import android.support.design.widget.FloatingActionButton
import android.support.design.widget.Snackbar
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View

import com.roughike.bottombar.BottomBar
import com.roughike.bottombar.OnMenuTabClickListener

import info.papdt.express.helper.R
import info.papdt.express.helper.dao.PackageDatabase
import info.papdt.express.helper.model.Package
import info.papdt.express.helper.support.CheatSheet
import info.papdt.express.helper.support.PushUtils
import info.papdt.express.helper.support.Settings
import info.papdt.express.helper.ui.common.AbsActivity
import info.papdt.express.helper.ui.fragment.home.BaseFragment
import info.papdt.express.helper.ui.fragment.home.FragmentAll
import info.papdt.express.helper.ui.launcher.AppWidgetProvider

class MainActivity : AbsActivity(), OnMenuTabClickListener {

	private val mBottomBar: BottomBar by lazy { BottomBar.attach(this, savedInstanceState) }
	private val fragments: Array<BaseFragment> = arrayOf(
			FragmentAll.newInstance(mDatabase, FragmentAll.TYPE_ALL),
			FragmentAll.newInstance(mDatabase, FragmentAll.TYPE_DELIVERED),
			FragmentAll.newInstance(mDatabase, FragmentAll.TYPE_DELIVERING)
	)

	private val TAG = "express.MainActivity"

	private val mDatabase: PackageDatabase by lazy { PackageDatabase.getInstance(applicationContext) }
	private var mContextMenuPackage: Package? = null

	override fun onCreate(savedInstanceState: Bundle?) {
		Log.i(TAG, "MainActivity launch")
		super.onCreate(savedInstanceState)

		if (settings.getBoolean(Settings.KEY_NAVIGATION_TINT, true)
				&& Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && !isNightMode) {
			window.navigationBarColor = resources.getColor(R.color.colorPrimaryDark)
		}

		/** Dirty fix for N  */
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
			PushUtils.startServices(applicationContext)
		}

		setContentView(R.layout.activity_main)

		mBottomBar.setItems(R.menu.bottombar_menu_home)
		mBottomBar.setOnMenuTabClickListener(this)

		if (ScannerActivity.ACTION_SCAN_TO_ADD.equals(intent.action)) {
			val intent = Intent(this@MainActivity, AddActivity::class.java)
			intent.action = ScannerActivity.ACTION_SCAN_TO_ADD
			intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
			intent.putExtra(AddActivity.EXTRA_IS_FROM_MAIN_ACTIVITY, true)
			startActivityForResult(intent, REQUEST_ADD)
		}
	}

	override fun setUpViews() {
		val fab = `$`<FloatingActionButton>(R.id.fab)
		fab!!.setOnClickListener {
			val intent = Intent(this@MainActivity, AddActivity::class.java)
			intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
			intent[AddActivity.EXTRA_IS_FROM_MAIN_ACTIVITY] = true
			startActivityForResult(intent, REQUEST_ADD)
		}
		CheatSheet.setup(fab)
	}

	override fun onSaveInstanceState(outState: Bundle) {
		super.onSaveInstanceState(outState)
		mBottomBar!!.onSaveInstanceState(outState)
	}

	override fun onStop() {
		super.onStop()
		mDatabase.save()
	}

	override fun onMenuTabSelected(@IdRes menuItemId: Int) {
		fragmentManager.executePendingTransactions()
		val ft = fragmentManager.beginTransaction()
		ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
		when (menuItemId) {
			R.id.menu_item_all -> ft.replace(R.id.container, fragments!![0])
			R.id.menu_item_delivered -> ft.replace(R.id.container, fragments!![1])
			R.id.menu_item_on_the_way -> ft.replace(R.id.container, fragments!![2])
		}
		ft.commit()
	}

	override fun onMenuTabReSelected(@IdRes menuItemId: Int) {
		when (menuItemId) {
			R.id.menu_item_all -> fragments!![0].scrollToTop()
			R.id.menu_item_delivered -> fragments!![1].scrollToTop()
			R.id.menu_item_on_the_way -> fragments!![2].scrollToTop()
		}
	}

	override fun onCreateOptionsMenu(menu: Menu): Boolean {
		menuInflater.inflate(R.menu.menu_home, menu)
		menu.tintItemsColor(Color.WHITE)
		return super.onCreateOptionsMenu(menu)
	}

	override fun onOptionsItemSelected(item: MenuItem): Boolean {
		when (item.itemId) {
			R.id.action_settings -> {
				SettingsActivity.launch(this, SettingsActivity.FLAG_MAIN)
				return true
			}
			R.id.action_read_all -> {
				ReadAllTask().execute()
				return true
			}
			R.id.action_search -> {
				val menuButton = findViewById<View>(R.id.action_search)
				val location = IntArray(2)
				menuButton.getLocationOnScreen(location)
				SearchActivity.launch(this, location[0] + menuButton.height / 2, location[1] + menuButton.width / 2)
				return true
			}
			R.id.action_import_export -> {
				ImportExportActivity.launch(this)
				return true
			}
			else -> return super.onOptionsItemSelected(item)
		}
	}

	public override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent) {
		Log.i("Main", "result received, requestCode=$requestCode, resultCode=$resultCode")
		if (requestCode == REQUEST_ADD) {
			if (resultCode == RESULT_NEW_PACKAGE) {
				val jsonData = data.getStringExtra(AddActivity.RESULT_EXTRA_PACKAGE_JSON)
				val p = Package.buildFromJson(jsonData)
				if (p != null) {
					Log.i("Main", p.toJsonString())
					mDatabase.add(p)
					this.notifyDataChanged(-1)
				}
			}
		}
		if (requestCode == REQUEST_DETAILS) {
			when (resultCode) {
				RESULT_RENAMED -> notifyDataChanged(-1)
				RESULT_DELETED -> {
					notifyDataChanged(-1)
					val fragId = mBottomBar!!.getCurrentTabPosition()
					Snackbar.make(
							`$`(R.id.coordinator_layout)!!,
							String.format(getString(R.string.toast_item_removed), data.getStringExtra("title")),
							Snackbar.LENGTH_LONG
					)
							.setAction(R.string.toast_item_removed_action) { fragments!![fragId].onUndoActionClicked() }
							.show()
				}
			}
		}
	}

	fun onContextMenuCreate(pack: Package) {
		mContextMenuPackage = pack
	}

	override fun onContextItemSelected(item: MenuItem): Boolean {
		if (mContextMenuPackage == null) {
			return super.onContextItemSelected(item)
		}
		when (item.itemId) {
			R.id.action_set_unread -> {
				mContextMenuPackage!!.unreadNew = true
				val position = mDatabase.indexOf(mContextMenuPackage!!)
				if (position != -1) {
					mDatabase[position] = mContextMenuPackage!!
					notifyDataChanged(-1)
				}
				return true
			}
			R.id.action_share -> {
				val data = mContextMenuPackage
				val text = getString(R.string.share_info_format,
						data!!.name,
						data.number,
						data.companyChineseName,
						if (data.data.size > 0) data.data[0].context else "Unknown",
						if (data.data.size > 0) data.data[0].time else ""
				)

				val intent = Intent(Intent.ACTION_SEND)
				intent.type = "text/plain"
				intent[Intent.EXTRA_TEXT] = text
				startActivity(Intent.createChooser(intent, getString(R.string.dialog_share_title)))
				return true
			}
		}
		return super.onContextItemSelected(item)
	}

	var mHandler: Handler = object : Handler() {
		override fun handleMessage(msg: Message) {
			when (msg.what) {
				MSG_NOTIFY_DATA_CHANGED -> {
					AppWidgetProvider.updateManually(application)
					fragments.indices.filter { it != msg.arg1 }.forEach { fragments[it].notifyDataSetChanged() }
				}
				MSG_NOTIFY_ITEM_REMOVE -> Snackbar.make(
						`$`(R.id.coordinator_layout)!!,
						String.format(getString(R.string.toast_item_removed), msg.data.getString("title")),
						Snackbar.LENGTH_LONG
				)
						.setAction(R.string.toast_item_removed_action) { fragments[mBottomBar!!.getCurrentTabPosition()].onUndoActionClicked() }
						.show()
			}
		}
	}

	fun notifyDataChanged(fromFragId: Int) {
		val msg = Message()
		msg.what = MSG_NOTIFY_DATA_CHANGED
		msg.arg1 = fromFragId
		mHandler.sendMessage(msg)
	}

	private inner class ReadAllTask : AsyncTask<Void, Void, Int>() {

		override fun doInBackground(vararg voids: Void): Int? {
			var count = 0
			for (i in 0 until mDatabase.size()) {
				if (mDatabase[i].unreadNew) {
					count++
					mDatabase[i].unreadNew = false
				}
			}
			mDatabase.save()
			return count
		}

		override fun onPostExecute(count: Int?) {
			notifyDataChanged(-1)
			Snackbar.make(
					`$`(R.id.coordinator_layout)!!,
					getString(R.string.toast_all_read, count),
					Snackbar.LENGTH_LONG
			).show()
		}

	}

	companion object {

		const val REQUEST_ADD = 10001
		const val RESULT_NEW_PACKAGE = 2000
		const val REQUEST_DETAILS = 10002
		const val RESULT_DELETED = 2001
		const val RESULT_RENAMED = 2002

		const val MSG_NOTIFY_DATA_CHANGED = 1
		const val MSG_NOTIFY_ITEM_REMOVE = 2

		fun launch(activity: Activity) {
			val intent = Intent(activity, MainActivity::class.java)
			intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
			activity.startActivity(intent)
		}

	}

}
