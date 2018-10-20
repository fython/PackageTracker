package info.papdt.express.helper.ui

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.Rect
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.support.design.widget.FloatingActionButton
import android.support.design.widget.Snackbar
import android.support.design.widget.TabLayout
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter
import android.support.v4.view.ViewPager
import android.support.v4.content.ContextCompat
import android.support.v7.app.ActionBar
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewTreeObserver
import android.widget.ImageButton
import com.getkeepsafe.taptargetview.TapTarget
import com.getkeepsafe.taptargetview.TapTargetSequence
import info.papdt.express.helper.*
import info.papdt.express.helper.R

import info.papdt.express.helper.dao.PackageDatabase
import info.papdt.express.helper.model.Kuaidi100Package
import info.papdt.express.helper.support.*
import info.papdt.express.helper.ui.common.AbsActivity
import info.papdt.express.helper.ui.fragment.home.FragmentAll
import info.papdt.express.helper.ui.launcher.AppWidgetProvider
import kotlinx.coroutines.experimental.Deferred
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.async
import moe.feng.kotlinyan.common.*
import org.jetbrains.anko.coroutines.experimental.bg

class MainActivity : AbsActivity() {

	private val tabLayout: TabLayout by lazyFindNonNullView(R.id.tab_layout)
	private val viewPager: ViewPager by lazyFindNonNullView(R.id.view_pager)
	private val toolbarBox: View by lazyFindNonNullView(R.id.toolbar_box)
	private val fab: FloatingActionButton by lazyFindNonNullView(R.id.fab)

	private val fragments by lazy { arrayOf(
			FragmentAll.newInstance(mDatabase, FragmentAll.TYPE_DELIVERING),
			FragmentAll.newInstance(mDatabase, FragmentAll.TYPE_DELIVERED),
			FragmentAll.newInstance(mDatabase, FragmentAll.TYPE_ALL)
	) }

	private val TAG = "express.MainActivity"

	private val mDatabase: PackageDatabase by lazy { PackageDatabase.getInstance(applicationContext) }
	private var mContextMenuPackage: Kuaidi100Package? = null

	@SuppressLint("NewApi")
	override fun onCreate(savedInstanceState: Bundle?) {
		Log.i(TAG, "MainActivity launch")
		super.onCreate(savedInstanceState)

		if (settings.getBoolean(Settings.KEY_NAVIGATION_TINT, true)) {
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && !isNightMode) {
				window.navigationBarColor = ContextCompat.getColor(this, R.color.colorPrimaryDark)
			}
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
				if (!isNightMode) {
					window.navigationBarColor = Color.WHITE
					ifSupportSDK (Build.VERSION_CODES.P) {
						window.navigationBarDividerColor = Color.argb(30, 0, 0, 0)
					}
					window.decorView.systemUiVisibility =
							window.decorView.systemUiVisibility or View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR
				} else {
					window.navigationBarColor = ResourcesUtils.getColorIntFromAttr(theme, android.R.attr.windowBackground)
					ifSupportSDK (Build.VERSION_CODES.P) {
						window.navigationBarDividerColor = Color.argb(60, 255, 255, 255)
					}
				}
			}
		}

		/** Dirty fix for N  */
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
			PushUtils.startServices(applicationContext)
		}

		setContentView(R.layout.activity_main)

		supportActionBar?.displayOptions = ActionBar.DISPLAY_SHOW_CUSTOM

		// Set up toolbar
		mToolbar?.setOnClickListener { startSearch() }
		mToolbar?.setOnLongClickListener { startSearch(isLongClick = true); true }

		// Set up views
		tabLayout.setupWithViewPager(viewPager)
		tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
			override fun onTabReselected(tab: TabLayout.Tab?) { fragments[viewPager.currentItem].scrollToTop() }
			override fun onTabUnselected(tab: TabLayout.Tab?) {}
			override fun onTabSelected(tab: TabLayout.Tab?) {}
		})
		viewPager.adapter = TabsAdapter(supportFragmentManager)
		viewPager.offscreenPageLimit = 3
		fab.setOnClickListener { startSearch() }

		// Do action
		when (intent.action) {
			ScannerActivity.ACTION_SCAN_TO_ADD -> openScanner()
			ACTION_SEARCH -> startSearch(intent[EXTRA_SEARCH]?.asString())
		}
	}

	override fun setUpViews() {}

	override fun onStop() {
		super.onStop()
		mDatabase.save()
	}

	private fun showTapTargetTips() {
		if (!SettingsInstance.shouldShowTips) return
		// Show tap target views
		val targets = listOf(
				TapTarget.forView(
						findViewById<ImageButton>(R.id.action_start_search),
						resources.string[R.string.tap_target_tips_1_title],
						resources.string[R.string.tap_target_tips_1_desc]
				).tintTarget(true).targetCircleColor(R.color.white_in_dark),
				TapTarget.forToolbarMenuItem(
						mToolbar,
						R.id.action_scan,
						resources.string[R.string.tap_target_tips_2_title],
						resources.string[R.string.tap_target_tips_2_desc]
				).tintTarget(true).targetCircleColor(R.color.white_in_dark)
		)
		TapTargetSequence(this)
				.targets(targets)
				.continueOnCancel(true)
				.listener(object : TapTargetSequence.Listener {
					override fun onSequenceCanceled(lastTarget: TapTarget?) {}
					override fun onSequenceStep(lastTarget: TapTarget?, targetClicked: Boolean) {}
					override fun onSequenceFinish() {
						val currentFragment = fragments[viewPager.currentItem]
						currentFragment.isDemoRefresh = true
						currentFragment.mRefreshLayout.autoRefresh()
						TapTargetSequence(this@MainActivity)
								.target(TapTarget.forBounds(
										Rect(
												// Left
												(window.decorView.width / 2)
														- 64f.dpToPx(this@MainActivity).toInt(),
												// Top
												tabLayout.bottom
														+ ScreenUtils.getStatusBarHeight(this@MainActivity)
														+ currentFragment.mSwipeHeader.measuredHeight,
												// Right
												(window.decorView.width / 2)
														+ 64f.dpToPx(this@MainActivity).toInt(),
												// Bottom
												tabLayout.bottom
														+ ScreenUtils.getStatusBarHeight(this@MainActivity)
														+ currentFragment.mSwipeHeader.measuredHeight
														+ 128f.dpToPx(this@MainActivity).toInt()
										),
										resources.string[R.string.tap_target_tips_3_title],
										resources.string[R.string.tap_target_tips_3_desc]
								).targetRadius(100).outerCircleColor(R.color.colorAccent).transparentTarget(true))
								.listener(object : TapTargetSequence.Listener {
									override fun onSequenceCanceled(lastTarget: TapTarget?) {}
									override fun onSequenceStep(lastTarget: TapTarget?, targetClicked: Boolean) {}
									override fun onSequenceFinish() {
										fragments[viewPager.currentItem].mRefreshLayout.finishRefresh()
										SettingsInstance.shouldShowTips = false
									}
								})
								.continueOnCancel(true)
								.start()
					}
				})
				.start()
	}

	override fun onCreateOptionsMenu(menu: Menu): Boolean {
		val observer = mToolbar?.viewTreeObserver
		observer?.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
			override fun onGlobalLayout() {
				observer.removeOnGlobalLayoutListener(this)
				if (observer.isAlive) showTapTargetTips()
			}
		})
		menuInflater.inflate(R.menu.menu_home, menu)
		menu.tintItemsColor(resources.color[R.color.black_in_light])
		return super.onCreateOptionsMenu(menu)
	}

	override fun onOptionsItemSelected(item: MenuItem): Boolean {
		when (item.itemId) {
			R.id.action_settings -> {
				SettingsActivity.launch(this, SettingsActivity.FLAG_MAIN)
				return true
			}
			R.id.action_read_all -> {
				async(UI) {
					val data: Deferred<Int> = bg {
						var count = 0
						for (i in 0 until mDatabase.size()) {
							if (mDatabase[i].unreadNew) {
								count++
								mDatabase[i].unreadNew = false
							}
						}
						mDatabase.save()
						count
					}

					notifyDataChanged(-1)
					Snackbar.make(
							findViewById(R.id.coordinator_layout),
							getString(R.string.toast_all_read, data.await()),
							Snackbar.LENGTH_LONG
					).show()
				}
				return true
			}
			R.id.action_scan -> {
				openScanner()
				return true
			}
			R.id.action_import_export -> {
				ImportExportActivity.launch(this)
				return true
			}
			else -> return super.onOptionsItemSelected(item)
		}
	}

	private fun openScanner() {
		val intent = Intent(this, ScannerActivity::class.java)
		intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
		startActivityForResult(intent, ScannerActivity.REQUEST_CODE_SCAN)
	}

	private fun startSearch(keyword: String? = null, isLongClick: Boolean = false) {
		val location = IntArray(2)
		toolbarBox.getLocationOnScreen(location)
		SearchActivity.launch(this,
				window.decorView.width / 2, location[0] + toolbarBox.height / 2, keyword, isLongClick)
	}

	public override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
		Log.i("Main", "result received, requestCode=$requestCode, resultCode=$resultCode")
		if (requestCode == REQUEST_ADD) {
			if (resultCode == RESULT_NEW_PACKAGE) {
				this.notifyDataChanged(-1)
			}
		}
		if (requestCode == REQUEST_DETAILS) {
			when (resultCode) {
				RESULT_RENAMED -> notifyDataChanged(-1)
				RESULT_DELETED -> {
					notifyDataChanged(-1)
					val fragId = viewPager.currentItem
					Snackbar.make(
							findViewById(R.id.coordinator_layout)!!,
							String.format(getString(R.string.toast_item_removed), data!!["title"]?.asString()),
							Snackbar.LENGTH_LONG
					)
							.setAction(R.string.toast_item_removed_action) { fragments[fragId].onUndoActionClicked() }
							.show()
				}
			}
		}
		if (requestCode == ScannerActivity.REQUEST_CODE_SCAN) {
			if (resultCode == RESULT_OK) {
				startSearch(data!![ScannerActivity.EXTRA_RESULT]?.asString())
			}
		}
	}

	fun onContextMenuCreate(pack: Kuaidi100Package) {
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
						if (data.data!!.size > 0) data.data!![0].context else "Unknown",
						if (data.data!!.size > 0) data.data!![0].time else ""
				)

				val intent = Intent(Intent.ACTION_SEND)
				intent.type = "text/plain"
				intent[Intent.EXTRA_TEXT] = text
				startActivity(Intent.createChooser(intent, getString(R.string.dialog_share_title)))
				return true
			}
			R.id.action_delete -> {
				val title = mContextMenuPackage!!.name
				mDatabase.remove(mContextMenuPackage!!)
				val msg = Message()
				msg.what = MainActivity.MSG_NOTIFY_ITEM_REMOVE
				msg.arg1 = -1
				val data = Bundle()
				data.putString("title", title)
				msg.data = data
				mHandler.sendMessage(msg)
				notifyDataChanged(-1)
			}
		}
		return super.onContextItemSelected(item)
	}

	@SuppressLint("HandlerLeak")
	val mHandler: Handler = object : Handler() {
		override fun handleMessage(msg: Message) {
			when (msg.what) {
				MSG_NOTIFY_DATA_CHANGED -> {
					AppWidgetProvider.updateManually(application)
					fragments.indices.filter { it != msg.arg1 }.forEach { fragments[it].notifyDataSetChanged() }
				}
				MSG_NOTIFY_ITEM_REMOVE -> Snackbar.make(
						findViewById(R.id.coordinator_layout)!!,
						String.format(getString(R.string.toast_item_removed), msg.data.getString("title")),
						Snackbar.LENGTH_LONG
				)
						.setAction(R.string.toast_item_removed_action) { fragments[viewPager.currentItem].onUndoActionClicked() }
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

	private inner class TabsAdapter(fm: FragmentManager): FragmentPagerAdapter(fm) {

		override fun getItem(position: Int): Fragment = fragments[position]
		override fun getCount(): Int = fragments.size
		override fun getPageTitle(position: Int): CharSequence? = when (position) {
			0 -> resources.string[R.string.navigation_item_on_the_way]
			1 -> resources.string[R.string.navigation_item_delivered]
			2 -> resources.string[R.string.navigation_item_all]
			else -> null
		}

	}

	companion object {

		const val MSG_NOTIFY_DATA_CHANGED = 1
		const val MSG_NOTIFY_ITEM_REMOVE = 2

		private const val EXTRA_SEARCH = "search"

		fun launch(activity: Activity) {
			val intent = Intent(activity, MainActivity::class.java)
			intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
			activity.startActivity(intent)
		}

		fun search(context: Context, number: String? = null) {
			context.startActivity(getSearchIntent(context, number))
		}

		fun getSearchIntent(context: Context, number: String? = null): Intent {
			val intent = Intent(context, MainActivity::class.java)
			intent.action = ACTION_SEARCH
			intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
			intent[EXTRA_SEARCH] = number
			return intent
		}

	}

}
