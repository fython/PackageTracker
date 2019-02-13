package info.papdt.express.helper.ui

import android.annotation.SuppressLint
import android.app.Activity
import android.app.ActivityManager
import android.app.ProgressDialog
import android.content.Intent
import android.graphics.Color
import android.os.AsyncTask
import android.os.Bundle
import com.google.android.material.appbar.CollapsingToolbarLayout
import com.google.android.material.snackbar.Snackbar
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.coordinatorlayout.widget.CoordinatorLayout
import com.google.android.material.appbar.AppBarLayout

import info.papdt.express.helper.R
import info.papdt.express.helper.REQUEST_DETAILS
import info.papdt.express.helper.RESULT_DELETED
import info.papdt.express.helper.RESULT_RENAMED
import info.papdt.express.helper.api.PackageApi
import info.papdt.express.helper.dao.PackageDatabase
import info.papdt.express.helper.model.BaseMessage
import info.papdt.express.helper.model.Kuaidi100Package
import info.papdt.express.helper.support.*
import info.papdt.express.helper.ui.common.AbsActivity
import info.papdt.express.helper.ui.dialog.EditPackageDialog
import info.papdt.express.helper.ui.items.DetailsStatusItemBinder
import info.papdt.express.helper.ui.items.DetailsTwoLineItem
import info.papdt.express.helper.ui.items.DetailsTwoLineItemBinder
import info.papdt.express.helper.ui.items.SubheaderItemBinder
import info.papdt.express.helper.view.SwipeBackCoordinatorLayout
import me.drakeet.multitype.Items
import me.drakeet.multitype.MultiTypeAdapter
import moe.feng.kotlinyan.common.*
import moe.feng.kotlinyan.common.lazyFindNonNullView
import org.jetbrains.anko.withAlpha
import kotlin.math.max
import kotlin.math.min

class DetailsActivity : AbsActivity() {

    private val mAppBarLayout: AppBarLayout by lazyFindNonNullView(R.id.app_bar_layout)
	private val mToolbarLayout: CollapsingToolbarLayout by lazyFindNonNullView(R.id.collapsing_layout)
	private val mRecyclerView: RecyclerView by lazyFindNonNullView(R.id.recycler_view)
	private val mBackground: ImageView by lazyFindNonNullView(R.id.parallax_background)
    private val mSwipeBackCoordinatorLayout: SwipeBackCoordinatorLayout by lazyFindNonNullView(R.id.swipe_back_coordinator_layout)
    private val mRootLayout: LinearLayout by lazyFindNonNullView(R.id.root_layout)

	private val mAdapter: MultiTypeAdapter by lazy {
		MultiTypeAdapter().apply {
			register(DetailsTwoLineItem::class.java, DetailsTwoLineItemBinder())
			register(String::class.java, SubheaderItemBinder())
			register(Kuaidi100Package.Status::class.java, mStatusBinder)
		}
	}
	private val mStatusBinder: DetailsStatusItemBinder by lazy { DetailsStatusItemBinder() }

	private val mDeleteDialog: AlertDialog by lazy {
		buildV7AlertDialog {
			titleRes = R.string.dialog_delete_title
			messageRes = R.string.dialog_delete_message
			okButton { _, _ ->
                val intent = Intent()
                intent.putExtra("data", data!!)
                setResult(RESULT_DELETED, intent)
				finish()
			}
			cancelButton()
		}
	}

	private var data: Kuaidi100Package? = null
	private var state: Int = 0

	private val progressDialog: ProgressDialog by lazy {
		ProgressDialog(this@DetailsActivity).apply {
			setMessage("")
			setCancelable(false)
		}
	}

	override fun onCreate(savedInstanceState: Bundle?) {
		window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_STABLE or
				View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
		window.statusBarColor = Color.TRANSPARENT

		super.onCreate(savedInstanceState)

		state = intent.getIntExtra(EXTRA_STATE, Kuaidi100Package.STATUS_FAILED)

		setContentView(R.layout.activity_details)

		setUpData()
	}

	override fun setUpViews() {
		mRecyclerView.setHasFixedSize(false)
		mRecyclerView.layoutManager = LinearLayoutManager(this)

        val rootViewBgColor = ResourcesUtils.getColorIntFromAttr(
                theme, R.attr.rootViewBackgroundColor)

        mSwipeBackCoordinatorLayout.setOnSwipeListener(object : SwipeBackCoordinatorLayout.OnSwipeListener {
            override fun canSwipeBack(dir: Int): Boolean {
                val behavior = (mAppBarLayout.layoutParams as CoordinatorLayout.LayoutParams).behavior
                return if (behavior is AppBarLayout.Behavior)
                    behavior.topAndBottomOffset == 0 else false
            }

            override fun onSwipeProcess(percent: Float) {
                mRootLayout.setBackgroundColor(
                        rootViewBgColor.withAlpha(
                                (SwipeBackCoordinatorLayout.getBackgroundAlpha(percent) * 255)
                                        .toInt()
                        )
                )
                mAppBarLayout.alpha = 1f - percent
                mRecyclerView.translationY = -min(max(0f, percent), 0.4f) * mAppBarLayout.height * 0.2f
            }

            override fun onSwipeFinish(dir: Int) {
                finish()
            }
        })
	}

	@SuppressLint("NewApi")
	internal fun setUpData() {
		mRecyclerView.adapter = mAdapter
		ListBuildTask().execute()

		val color: Int
		val colorDark: Int
		when (state) {
			Kuaidi100Package.STATUS_DELIVERED -> {
				mBackground.setImageResource(R.drawable.banner_background_delivered)
				color = resources.color[R.color.teal_500]
				colorDark = resources.color[R.color.teal_700]
			}
			Kuaidi100Package.STATUS_FAILED -> {
				mBackground.setImageResource(R.drawable.banner_background_error)
				color = resources.color[R.color.blue_grey_500]
				colorDark = resources.color[R.color.blue_grey_700]
			}
			else -> {
				mBackground.setImageResource(R.drawable.banner_background_on_the_way)
				color = resources.color[R.color.blue_500]
				colorDark = resources.color[R.color.blue_700]
			}
		}
		mToolbarLayout.setContentScrimColor(color)
		mToolbarLayout.setStatusBarScrimColor(colorDark)
	}

	private fun buildItems(): Items {
		val newItems = Items()
		newItems.add(DetailsTwoLineItem(DetailsTwoLineItem.TYPE_NAME, data!!.name!!))
		newItems.add(DetailsTwoLineItem(DetailsTwoLineItem.TYPE_NUMBER, data!!.number!!, data!!.companyChineseName))
		newItems.add(getString(R.string.list_status_subheader))
		newItems.addAll(data!!.data ?: listOf())
		return newItems
	}

	override fun onCreateOptionsMenu(menu: Menu): Boolean {
		menuInflater.inflate(R.menu.menu_details, menu)
		return super.onCreateOptionsMenu(menu)
	}

	override fun onOptionsItemSelected(item: MenuItem): Boolean {
		when (item.itemId) {
            R.id.action_edit -> {
                showNameEditDialog()
                return true
            }
			R.id.action_copy_code -> {
				ClipboardUtils.putString(applicationContext, data!!.number)
				Snackbar.make(findViewById(R.id.coordinator_layout)!!, R.string.toast_copied_code, Snackbar.LENGTH_LONG)
						.show()
				return true
			}
			R.id.action_share -> {
				showShareChooser()
				return true
			}
			R.id.action_delete -> {
				showDeleteDialog()
				return true
			}
			R.id.action_set_unread -> {
				data!!.unreadNew = true

				val intent = Intent()
				intent.putExtra("id", data!!.number)
				setResult(RESULT_RENAMED, intent)

				val db = PackageDatabase.getInstance(applicationContext)
				db[db.indexOf(data!!.number!!)] = data!!

				finish()
				return true
			}
			R.id.action_refresh -> {
				ui {
					progressDialog.show()
					val result = asyncIO {
						val newPack = PackageApi.getPackage(data!!.number!!, data!!.companyType)
						if (newPack.code != BaseMessage.CODE_OKAY || newPack.data?.data == null) {
							false
						} else {
							data!!.applyNewData(newPack.data)
							val db = PackageDatabase.getInstance(applicationContext)
							db[db.indexOf(data!!.number!!)] = data!!
							db.save()
							true
						}
					}.await()
					progressDialog.dismiss()
					if (result) {
						val intent = Intent()
						intent["id"] = data!!.number
						setResult(RESULT_RENAMED, intent)
						setUpData()
					}
				}
				return true
			}
			else -> return super.onOptionsItemSelected(item)
		}
	}

	fun showNameEditDialog() {
		EditPackageDialog.newInstance(data!!).show(supportFragmentManager, "edit")
	}

	private fun showDeleteDialog() {
		mDeleteDialog.show()
	}

	private fun showShareChooser() {
		val text = getString(R.string.share_info_format,
				data!!.name,
				data!!.number,
				data!!.companyChineseName,
				if (data!!.data!!.size > 0) data!!.data!![0].context else "Unknown",
				if (data!!.data!!.size > 0) data!!.data!![0].time else ""
		)

		val intent = Intent(Intent.ACTION_SEND)
		intent.type = "text/plain"
		intent[Intent.EXTRA_TEXT] = text
		startActivity(Intent.createChooser(intent, getString(R.string.dialog_share_title)))
	}

	private inner class ListBuildTask : AsyncTask<Void, Void, Items>() {

		override fun doInBackground(vararg voids: Void): Items {
			if (data == null) {
				data = Kuaidi100Package.buildFromJson(intent.getStringExtra(EXTRA_PACKAGE_JSON))
			}

			return buildItems()
		}

		override fun onPostExecute(items: Items) {
			mStatusBinder.setData(data)
			mAdapter.items = items
			mStatusBinder.showChiba =
					(data?.data?.find { it.context!!.contains("佛山") || it.context!!.contains("广州") } != null)
							&& (data?.companyChineseName?.contains("圆通") == true)
			mAdapter.notifyDataSetChanged()

			val color: Int = when (state) {
				Kuaidi100Package.STATUS_DELIVERED -> resources.color[R.color.teal_500]
				Kuaidi100Package.STATUS_FAILED -> resources.color[R.color.blue_grey_500]
				else -> resources.color[R.color.blue_500]
			}
            val taskDesc = ActivityManager.TaskDescription(data!!.name, null, color)
            setTaskDescription(taskDesc)
		}

	}

	companion object {

		private const val EXTRA_PACKAGE_JSON = "extra_package_json"
		private const val EXTRA_STATE = "extra_state"

		fun launch(activity: Activity, p: Kuaidi100Package) {
			val intent = Intent(activity, DetailsActivity::class.java)
			intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
			intent[EXTRA_PACKAGE_JSON] = p.toJsonString()
			intent[EXTRA_STATE] = p.getState()
			activity.startActivityForResult(intent, REQUEST_DETAILS)
		}
	}

}
