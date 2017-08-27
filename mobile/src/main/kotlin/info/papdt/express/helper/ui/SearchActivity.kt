package info.papdt.express.helper.ui

import android.animation.Animator
import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Color
import android.os.AsyncTask
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.support.annotation.RequiresApi
import android.support.v4.graphics.drawable.DrawableCompat
import android.support.v7.app.ActionBar
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.AppCompatEditText
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.text.Editable
import android.text.TextWatcher
import android.view.*
import android.view.inputmethod.EditorInfo

import java.util.ArrayList

import info.papdt.express.helper.R
import info.papdt.express.helper.api.PackageApi
import info.papdt.express.helper.asynctask.CompanyFilterTask
import info.papdt.express.helper.dao.PackageDatabase
import info.papdt.express.helper.model.Package
import info.papdt.express.helper.support.Settings
import info.papdt.express.helper.ui.adapter.SearchResultAdapter
import info.papdt.express.helper.ui.common.AbsActivity
import moe.feng.kotlinyan.common.AndroidExtensions
import moe.feng.kotlinyan.common.lazyFindNonNullView

class SearchActivity : AbsActivity() {

	private val mList: RecyclerView by lazyFindNonNullView(R.id.recycler_view)
	private val mSearchEdit: AppCompatEditText by lazy {
		AppCompatEditText(this@SearchActivity).apply {
			/** Create search edit widget  */
			setTextAppearance(this@SearchActivity, R.style.TextAppearance_AppCompat_Widget_ActionBar_Title)
			setSingleLine(true)
			setBackgroundColor(Color.TRANSPARENT)
			setHint(R.string.search_hint_common)
			imeOptions = EditorInfo.IME_ACTION_DONE
			addTextChangedListener(object : TextWatcher {
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
						CompanySearchTask().execute(charSequence.toString().trim { it <= ' ' })
						PackageSearchTask().execute(charSequence.toString().trim { it <= ' ' })
					}
				}

				override fun afterTextChanged(editable: Editable) {

				}
			})
		}
	}
	private val rootLayout: View by lazyFindNonNullView(R.id.root_layout)

	private lateinit var mAdapter: SearchResultAdapter

	private var companies: ArrayList<PackageApi.CompanyInfo.Company>? = null
	private var packages: ArrayList<Package>? = null

	private val mDatabase: PackageDatabase by lazy { PackageDatabase.getInstance(applicationContext) }

	public override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)

		if (settings.getBoolean(Settings.KEY_NAVIGATION_TINT, true)
				&& Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && !isNightMode) {
			window.navigationBarColor = resources.color[R.color.lollipop_status_bar_grey]
		}

		setContentView(R.layout.activity_search)

		if (savedInstanceState == null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
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
						}, 100)
						rootLayout.viewTreeObserver.removeOnGlobalLayoutListener(this)
					}
				})
			}
		}
	}

	@SuppressLint("NewApi")
	override fun setUpViews() {
		/** Set up custom view on ActionBar  */
		val lp = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
		mSearchEdit.layoutParams = lp

		val lp2 = ActionBar.LayoutParams(lp)
		mActionBar!!.setCustomView(mSearchEdit, lp2)

		mActionBar!!.setDisplayHomeAsUpEnabled(true)
		mActionBar!!.setDisplayShowCustomEnabled(true)
		mActionBar!!.setDisplayShowTitleEnabled(false)

		DrawableCompat.wrap(mToolbar!!.navigationIcon!!).setTint(resources.getColor(R.color.black_in_light))

		/** Set up company list  */
		mList.setHasFixedSize(true)
		mList.layoutManager = LinearLayoutManager(this)
		mAdapter = SearchResultAdapter(this)

		mAdapter.setCompanies(null)
		mAdapter.setPackages(null)

		mList.adapter = mAdapter
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
			override fun onAnimationEnd(animator: Animator) {
				rootLayout.makeInvisible()
				finish()
			}
		})
		circularReveal.duration = 400
		circularReveal.start()
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

	private inner class CompanySearchTask : CompanyFilterTask() {

		override fun onPostExecute(companies: ArrayList<PackageApi.CompanyInfo.Company>) {
			this@SearchActivity.companies = companies
			mAdapter.setCompanies(companies)
			mAdapter.setItems(buildItems())
			mAdapter.notifyDataSetChanged()
		}

	}

	private inner class PackageSearchTask : AsyncTask<String, Void, ArrayList<Package>>() {

		override fun doInBackground(vararg str: String): ArrayList<Package> {
			val keyword = str[0].trim { it <= ' ' }.toLowerCase()
			return (0 until mDatabase.size())
					.filter { mDatabase[it].name.toLowerCase().contains(keyword) || mDatabase[it].number.toLowerCase().contains(keyword) }
					.mapTo(ArrayList()) { mDatabase[it] }
		}

		override fun onPostExecute(packages: ArrayList<Package>) {
			this@SearchActivity.packages = packages
			mAdapter.setPackages(packages)
			mAdapter.setItems(buildItems())
			mAdapter.notifyDataSetChanged()
		}

	}

	companion object: AndroidExtensions {

		private const val EXTRA_CX = "cx"
		private const val EXTRA_CY = "cy"

		fun launch(activity: AppCompatActivity, cx: Int, cy: Int) {
			val intent = Intent(activity, SearchActivity::class.java)
			intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
			intent[EXTRA_CX] = cx
			intent[EXTRA_CY] = cy
			activity.startActivity(intent)
		}
	}

}
