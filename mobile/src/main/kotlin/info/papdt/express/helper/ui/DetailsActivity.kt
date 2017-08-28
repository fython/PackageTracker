package info.papdt.express.helper.ui

import android.app.ActivityManager
import android.app.ProgressDialog
import android.content.Intent
import android.graphics.Color
import android.graphics.PorterDuff
import android.os.AsyncTask
import android.os.Build
import android.os.Bundle
import android.support.design.widget.CollapsingToolbarLayout
import android.support.design.widget.FloatingActionButton
import android.support.design.widget.Snackbar
import android.support.v4.graphics.drawable.DrawableCompat
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.AppCompatEditText
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.text.TextUtils
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.ImageView
import cn.nekocode.rxlifecycle.RxLifecycle

import info.papdt.express.helper.R
import info.papdt.express.helper.api.PackageApi
import info.papdt.express.helper.dao.PackageDatabase
import info.papdt.express.helper.model.BaseMessage
import info.papdt.express.helper.model.Package
import info.papdt.express.helper.support.CheatSheet
import info.papdt.express.helper.support.ClipboardUtils
import info.papdt.express.helper.support.ScreenUtils
import info.papdt.express.helper.support.Settings
import info.papdt.express.helper.ui.common.AbsActivity
import info.papdt.express.helper.ui.items.DetailsStatusItemBinder
import info.papdt.express.helper.ui.items.DetailsTwoLineItem
import info.papdt.express.helper.ui.items.DetailsTwoLineItemBinder
import info.papdt.express.helper.ui.items.SubheaderItemBinder
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import me.drakeet.multitype.Items
import me.drakeet.multitype.MultiTypeAdapter
import moe.feng.kotlinyan.common.AndroidExtensions
import moe.feng.kotlinyan.common.lazyFindNonNullView
import kotlin.concurrent.thread

class DetailsActivity : AbsActivity() {

	private val mToolbarLayout: CollapsingToolbarLayout by lazyFindNonNullView(R.id.collapsing_layout)
	private val mRecyclerView: RecyclerView by lazyFindNonNullView(R.id.recycler_view)
	private val mFAB: FloatingActionButton by lazyFindNonNullView(R.id.fab)
	private val mBackground: ImageView by lazyFindNonNullView(R.id.parallax_background)
	private val mNameEdit: AppCompatEditText by lazy {
		AppCompatEditText(this@DetailsActivity).apply {
			setSingleLine(true)
		}
	}

	private val mAdapter: MultiTypeAdapter by lazy {
		MultiTypeAdapter().apply {
			register(DetailsTwoLineItem::class.java, DetailsTwoLineItemBinder())
			register(String::class.java, SubheaderItemBinder())
			register(Package.Status::class.java, mStatusBinder)
		}
	}
	private val mStatusBinder: DetailsStatusItemBinder by lazy { DetailsStatusItemBinder() }

	private val mEditDialog: AlertDialog by lazy {
		buildV7AlertDialog {
			val DP8 = ScreenUtils.dpToPx(context, 8f).toInt()
			titleRes = R.string.dialog_edit_name_title
			setView(mNameEdit, DP8, DP8, DP8, DP8)
			okButton { _, _ ->
				if (!TextUtils.isEmpty(mNameEdit.text.toString())) {
					data!!.name = mNameEdit.text.toString().trim { it <= ' ' }
					mAdapter.notifyDataSetChanged()

					val intent = Intent()
					intent["id"] = data!!.number
					setResult(MainActivity.RESULT_RENAMED, intent)

					thread {
						val db = PackageDatabase.getInstance(applicationContext)
						db[db.indexOf(data!!.number)] = data!!
						db.save()
					}.start()
				} else {
					Snackbar.make(`$`(R.id.coordinator_layout)!!, R.string.toast_edit_name_is_empty, Snackbar.LENGTH_SHORT)
							.show()
				}
			}
			cancelButton()
		}
	}
	private val mDeleteDialog: AlertDialog by lazy {
		buildV7AlertDialog {
			titleRes = R.string.dialog_delete_title
			messageRes = R.string.dialog_delete_message
			okButton { _, _ ->
				PackageDatabase.getInstance(applicationContext).remove(data!!)
				val intent = Intent()
				intent["title"] = data!!.name
				setResult(MainActivity.RESULT_DELETED, intent)
				finish()
			}
			cancelButton()
		}
	}

	private var data: Package? = null
	private var state: Int = 0

	private val progressDialog: ProgressDialog by lazy {
		ProgressDialog(this@DetailsActivity).apply {
			setMessage("")
			setCancelable(false)
		}
	}

	override fun onCreate(savedInstanceState: Bundle?) {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
			window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_STABLE or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
			window.statusBarColor = Color.TRANSPARENT
		}
		super.onCreate(savedInstanceState)

		state = intent.getIntExtra(EXTRA_STATE, Package.STATUS_FAILED)

		setContentView(R.layout.activity_details)

		mActionBar!!.setDisplayHomeAsUpEnabled(true)

		setUpData()
	}

	override fun setUpViews() {
		mRecyclerView.setHasFixedSize(false)
		mRecyclerView.layoutManager = LinearLayoutManager(this)

		mFAB.setOnClickListener { showNameEditDialog() }
		CheatSheet.setup(mFAB)
	}

	private fun setUpData() {
		mRecyclerView.adapter = mAdapter
		ListBuildTask().execute()

		var drawable = mFAB.drawable
		if (mFAB.drawable == null) {
			drawable = DrawableCompat.wrap(resources.getDrawable(R.drawable.ic_create_black_24dp))
			DrawableCompat.setTintMode(drawable, PorterDuff.Mode.SRC_IN)
			mFAB.setImageDrawable(drawable)
		}

		val color: Int
		val colorDark: Int
		when (state) {
			Package.STATUS_DELIVERED -> {
				mBackground.setImageResource(R.drawable.banner_background_delivered)
				color = resources.color[R.color.teal_500]
				colorDark = resources.color[R.color.teal_700]
			}
			Package.STATUS_FAILED -> {
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
		DrawableCompat.setTint(drawable, color)
		if (settings.getBoolean(Settings.KEY_NAVIGATION_TINT, true)
				&& Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && !isNightMode) {
			window.navigationBarColor = colorDark
		}
	}

	private fun buildItems(): Items {
		val newItems = Items()
		newItems.add(DetailsTwoLineItem(DetailsTwoLineItem.TYPE_NAME, data!!.name))
		newItems.add(DetailsTwoLineItem(DetailsTwoLineItem.TYPE_NUMBER, data!!.number, data!!.companyChineseName))
		newItems.add(getString(R.string.list_status_subheader))
		newItems.addAll(data!!.data)
		return newItems
	}

	override fun onCreateOptionsMenu(menu: Menu): Boolean {
		menuInflater.inflate(R.menu.menu_details, menu)
		return super.onCreateOptionsMenu(menu)
	}

	override fun onOptionsItemSelected(item: MenuItem): Boolean {
		val id = item.itemId
		when (id) {
			R.id.action_copy_code -> {
				ClipboardUtils.putString(applicationContext, data!!.number)
				Snackbar.make(`$`(R.id.coordinator_layout)!!, R.string.toast_copied_code, Snackbar.LENGTH_LONG)
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
				setResult(MainActivity.RESULT_RENAMED, intent)

				val db = PackageDatabase.getInstance(applicationContext)
				db[db.indexOf(data!!.number)] = data!!

				finish()
				return true
			}
			R.id.action_refresh -> {
				Observable.just("")
						.compose(RxLifecycle.bind(this).withObservable())
						.map {
							val newPack = PackageApi.getPackage(data!!.companyType, data!!.number)
							if (newPack.code != BaseMessage.CODE_OKAY || newPack.data?.data == null) {
								false
							} else {
								data!!.applyNewData(newPack.data)
								val db = PackageDatabase.getInstance(applicationContext)
								db[db.indexOf(data!!.number)] = data!!
								db.save()
								true
							}
						}
						.subscribeOn(Schedulers.io())
						.observeOn(AndroidSchedulers.mainThread())
						.doOnSubscribe { progressDialog.show() }
						.subscribe { isSucceed ->
							progressDialog.dismiss()
							if (isSucceed) {
								val intent = Intent()
								intent["id"] = data!!.number
								setResult(MainActivity.RESULT_RENAMED, intent)
								setUpData()
							}
						}
				return true
			}
			else -> return super.onOptionsItemSelected(item)
		}
	}

	fun showNameEditDialog() {
		mNameEdit.setText(data!!.name)
		mNameEdit.setSelection(data!!.name.length)
		mEditDialog.show()
	}

	private fun showDeleteDialog() {
		mDeleteDialog.show()
	}

	private fun showShareChooser() {
		val text = getString(R.string.share_info_format,
				data!!.name,
				data!!.number,
				data!!.companyChineseName,
				if (data!!.data.size > 0) data!!.data[0].context else "Unknown",
				if (data!!.data.size > 0) data!!.data[0].time else ""
		)

		val intent = Intent(Intent.ACTION_SEND)
		intent.type = "text/plain"
		intent[Intent.EXTRA_TEXT] = text
		startActivity(Intent.createChooser(intent, getString(R.string.dialog_share_title)))
	}

	private inner class ListBuildTask : AsyncTask<Void, Void, Items>() {

		override fun doInBackground(vararg voids: Void): Items {
			if (data == null) {
				data = Package.buildFromJson(intent.getStringExtra(EXTRA_PACKAGE_JSON))
			}

			val db = PackageDatabase.getInstance(applicationContext)
			if (data!!.unreadNew) {
				data!!.unreadNew = false

				val intent = Intent()
				intent.putExtra("id", data!!.number)
				setResult(MainActivity.RESULT_RENAMED, intent)
			}
			db[db.indexOf(data!!.number)] = data!!
			db.save()

			return buildItems()
		}

		override fun onPostExecute(items: Items) {
			mStatusBinder.setData(data)
			mAdapter.items = items
			mAdapter.notifyDataSetChanged()

			val color: Int = when (state) {
				Package.STATUS_DELIVERED -> resources.color[R.color.teal_500]
				Package.STATUS_FAILED -> resources.color[R.color.blue_grey_500]
				else -> resources.color[R.color.blue_500]
			}
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
				val taskDesc = ActivityManager.TaskDescription(data!!.name, null, color)
				setTaskDescription(taskDesc)
			}
		}

	}

	companion object: AndroidExtensions {

		private const val EXTRA_PACKAGE_JSON = "extra_package_json"
		private const val EXTRA_STATE = "extra_state"

		fun launch(activity: AppCompatActivity, p: Package) {
			val intent = Intent(activity, DetailsActivity::class.java)
			intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
			intent[EXTRA_PACKAGE_JSON] = p.toJsonString()
			intent[EXTRA_STATE] = p.state
			activity.startActivityForResult(intent, MainActivity.REQUEST_DETAILS)
		}
	}

}
