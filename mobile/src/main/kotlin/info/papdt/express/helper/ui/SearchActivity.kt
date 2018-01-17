package info.papdt.express.helper.ui

import android.animation.Animator
import android.annotation.SuppressLint
import android.content.ClipDescription
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.support.annotation.RequiresApi
import android.support.design.widget.Snackbar
import android.support.v7.app.ActionBar
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.AppCompatEditText
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.text.Editable
import android.text.TextWatcher
import android.view.*
import cn.nekocode.rxlifecycle.RxLifecycle

import java.util.ArrayList

import info.papdt.express.helper.R
import info.papdt.express.helper.REQUEST_ADD
import info.papdt.express.helper.RESULT_EXTRA_PACKAGE_JSON
import info.papdt.express.helper.RESULT_NEW_PACKAGE
import info.papdt.express.helper.api.Kuaidi100PackageApi
import info.papdt.express.helper.api.RxPackageApi
import info.papdt.express.helper.dao.PackageDatabase
import info.papdt.express.helper.model.Kuaidi100Package
import info.papdt.express.helper.support.Settings
import info.papdt.express.helper.ui.adapter.SearchResultAdapter
import info.papdt.express.helper.ui.common.AbsActivity
import info.papdt.express.helper.ui.fragment.add.AddDialogFragment
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import moe.feng.kotlinyan.common.*

class SearchActivity : AbsActivity(), AddDialogFragment.IAddDialogObserver {

	private val mList: RecyclerView by lazyFindNonNullView(R.id.recycler_view)
	private val mSearchEdit: AppCompatEditText by lazyFindNonNullView(R.id.search_edit)
	private val rootLayout: View by lazyFindNonNullView(R.id.root_layout)

	private lateinit var mAdapter: SearchResultAdapter

	private var companies: ArrayList<Kuaidi100PackageApi.CompanyInfo.Company>? = null
	private var packages: ArrayList<Kuaidi100Package>? = null

	private val mDatabase: PackageDatabase by lazy { PackageDatabase.getInstance(applicationContext) }

	public override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)

		if (settings.getBoolean(Settings.KEY_NAVIGATION_TINT, true)
				&& Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && !isNightMode) {
			window.navigationBarColor = resources.color[R.color.lollipop_status_bar_grey]
		}

		setContentView(R.layout.activity_search)

		if (savedInstanceState == null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
			intent[EXTRA_KEYWORD]?.asString()?.let { mSearchEdit.setText(it) }

			rootLayout.makeInvisible()

			val viewTreeObserver = rootLayout.viewTreeObserver
			if (viewTreeObserver.isAlive) {
				viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
					override fun onGlobalLayout() {
						Handler().postDelayed({
							ifSupportSDK (Build.VERSION_CODES.LOLLIPOP) {
								overridePendingTransition(R.anim.do_not_move, R.anim.do_not_move)
								window.decorView.systemUiVisibility = if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M || isNightMode)
									View.SYSTEM_UI_FLAG_LAYOUT_STABLE or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
								else
									View.SYSTEM_UI_FLAG_LAYOUT_STABLE or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
								window.statusBarColor = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
									Color.TRANSPARENT else resources.color[R.color.lollipop_status_bar_grey]
							}
							circularRevealActivity()
							if (intent[EXTRA_IS_LONGCLICK]!!.asBoolean()
									&& clipboardManager
									?.primaryClipDescription?.hasMimeType(ClipDescription.MIMETYPE_TEXT_PLAIN) == true) {
								Snackbar.make(rootLayout, R.string.toast_clipboard_paste, Snackbar.LENGTH_LONG)
										.setAction(R.string.toast_clipboard_paste_action) {
											mSearchEdit.onTextContextMenuItem(android.R.id.paste)
										}
										.show()
							}
						}, 100)
						rootLayout.viewTreeObserver.removeOnGlobalLayoutListener(this)
					}
				})
			}
		}
	}

	@SuppressLint("NewApi")
	override fun setUpViews() {
		mActionBar?.displayOptions = ActionBar.DISPLAY_SHOW_CUSTOM

		findViewById<View>(R.id.action_back).setOnClickListener { onBackPressed() }
		mSearchEdit.addTextChangedListener(object : TextWatcher {
			override fun beforeTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {}

			override fun onTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {
				if (charSequence.isNotEmpty()) {
					companies = null
					packages = null
				} else {
					companies = ArrayList()
					packages = ArrayList()
				}
				mAdapter.setCompanies(companies)
				mAdapter.setPackages(packages)
				mAdapter.setItems(buildItems())
				mAdapter.notifyDataSetChanged()
				if (charSequence.isNotEmpty()) {
					RxPackageApi.filterCompany(
							charSequence.toString().trim(),
							parentActivity = this@SearchActivity
					).subscribe { companies ->
						this@SearchActivity.companies = companies
						mAdapter.setCompanies(companies)
						mAdapter.setItems(buildItems())
						mAdapter.notifyDataSetChanged()
					}
					startFindPackage()
				}
			}

			override fun afterTextChanged(editable: Editable) {

			}
		})

		/** Set up company list  */
		mList.setHasFixedSize(true)
		mList.layoutManager = LinearLayoutManager(this)
		mAdapter = SearchResultAdapter(this)

		mAdapter.setCompanies(null)
		mAdapter.setPackages(null)

		mList.adapter = mAdapter
	}

	private fun startFindPackage() {
		Observable.just(mSearchEdit.text.toString().trim())
				.compose(RxLifecycle.bind(this@SearchActivity).withObservable())
				.map {
					val keyword = it.trim().toLowerCase()
					(0 until mDatabase.size())
							.filter { mDatabase[it].name!!.toLowerCase().contains(keyword) || mDatabase[it].number!!.toLowerCase().contains(keyword) }
							.mapTo(ArrayList()) { mDatabase[it] }
				}
				.subscribeOn(Schedulers.computation())
				.observeOn(AndroidSchedulers.mainThread())
				.subscribe { packages ->
					this@SearchActivity.packages = packages
					mAdapter.setPackages(packages)
					mAdapter.setItems(buildItems())
					mAdapter.notifyDataSetChanged()
				}
	}

	override fun onPackageAdd(p: Kuaidi100Package) {
		startFindPackage()
		val intent = Intent()
		intent.putExtra(RESULT_EXTRA_PACKAGE_JSON, p.toJsonString())
		setResult(RESULT_NEW_PACKAGE, intent)
	}

	@RequiresApi(Build.VERSION_CODES.LOLLIPOP)
	private fun circularRevealActivity() {
		val intent = intent

		val cx = intent[EXTRA_CX]?.asInt() ?: (rootLayout.width / 2)
		val cy = intent[EXTRA_CY]?.asInt() ?: (rootLayout.height / 2)

		val finalRadius = Math.max(rootLayout.width, rootLayout.height).toFloat()

		// create the animator for this view (the start radius is zero)
		val circularReveal = ViewAnimationUtils.createCircularReveal(rootLayout, cx, cy, 0f, finalRadius)
		circularReveal.duration = 300
		circularReveal.addListener(object : Animator.AnimatorListener {
			override fun onAnimationRepeat(p0: Animator?) {}
			override fun onAnimationEnd(p0: Animator?) { mSearchEdit.showKeyboard() }
			override fun onAnimationCancel(p0: Animator?) {}
			override fun onAnimationStart(p0: Animator?) {}
		})

		// make the view visible and start the animation
		rootLayout.makeVisible()
		circularReveal.start()
	}

	override fun onBackPressed() {
		val intent = intent

		val cx = intent[EXTRA_CX]?.asInt() ?: (rootLayout.width / 2)
		val cy = intent[EXTRA_CY]?.asInt() ?: (rootLayout.height / 2)

		val finalRadius = Math.max(rootLayout.width, rootLayout.height).toFloat()
		val circularReveal = ViewAnimationUtils.createCircularReveal(rootLayout, cx, cy, finalRadius, 0f)

		circularReveal.addListener(object : Animator.AnimatorListener {
			override fun onAnimationStart(animator: Animator) {}
			override fun onAnimationCancel(animator: Animator) {}
			override fun onAnimationRepeat(animator: Animator) {}
			override fun onAnimationEnd(animator: Animator) { rootLayout.makeInvisible(); finish() }
		})
		circularReveal.duration = 400
		circularReveal.start()
	}

	fun onAddButtonClicked() {
		mSearchEdit.hideKeyboard()
		AddDialogFragment.newInstance(mSearchEdit.text.toString()).show(supportFragmentManager, "add_dialog")
	}

	override fun onCreateOptionsMenu(menu: Menu): Boolean {
		menuInflater.inflate(R.menu.menu_search, menu)
		menu.tintItemsColor(resources.color[R.color.black_in_light])
		return super.onCreateOptionsMenu(menu)
	}

	override fun onOptionsItemSelected(item: MenuItem): Boolean {
		if (item.itemId == R.id.action_clear) {
			mSearchEdit.setText("")
			return true
		}
		return super.onOptionsItemSelected(item)
	}

	@Synchronized private fun buildItems(): ArrayList<SearchResultAdapter.ItemType> {
		val items = ArrayList<SearchResultAdapter.ItemType>()
		items.add(SearchResultAdapter.ItemType(SearchResultAdapter.ItemType.TYPE_SUBHEADER))
		if (packages?.size ?: 0 > 0) {
			items.addAll(packages!!.indices.map {
				SearchResultAdapter.ItemType(SearchResultAdapter.ItemType.TYPE_PACKAGE).apply { index = it }
			})
		} else {
			items.add(SearchResultAdapter.ItemType(SearchResultAdapter.ItemType.TYPE_EMPTY))
		}
		if (mSearchEdit.text.isNotBlank() && mDatabase.indexOf(mSearchEdit.text.toString().trim()) == -1) {
			items.add(SearchResultAdapter.ItemType(SearchResultAdapter.ItemType.TYPE_NEW_PACKAGE))
		}
		items.add(SearchResultAdapter.ItemType(SearchResultAdapter.ItemType.TYPE_SUBHEADER))
		if (companies?.size ?: 0 > 0) {
			items.addAll(companies!!.indices.map {
				SearchResultAdapter.ItemType(SearchResultAdapter.ItemType.TYPE_COMPANY).apply { index = it }
			})
		} else {
			items.add(SearchResultAdapter.ItemType(SearchResultAdapter.ItemType.TYPE_EMPTY))
		}
		return items
	}

	companion object {

		private const val EXTRA_CX = "cx"
		private const val EXTRA_CY = "cy"
		private const val EXTRA_KEYWORD = "keyword"
		private const val EXTRA_IS_LONGCLICK = "is_longclick"

		fun launch(activity: AppCompatActivity, cx: Int, cy: Int, keyword: String? = null, isLongClick: Boolean = false) {
			val intent = Intent(activity, SearchActivity::class.java)
			intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
			intent[EXTRA_CX] = cx
			intent[EXTRA_CY] = cy
			intent[EXTRA_KEYWORD] = keyword
			intent[EXTRA_IS_LONGCLICK] = isLongClick
			activity.startActivityForResult(intent, REQUEST_ADD)
		}

	}

}
