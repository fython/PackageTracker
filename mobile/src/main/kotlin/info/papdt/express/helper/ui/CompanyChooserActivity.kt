package info.papdt.express.helper.ui

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import androidx.appcompat.app.ActionBar
import androidx.appcompat.widget.AppCompatEditText
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo

import java.util.ArrayList

import info.papdt.express.helper.R
import info.papdt.express.helper.RESULT_EXTRA_COMPANY_CODE
import info.papdt.express.helper.api.Kuaidi100PackageApi
import info.papdt.express.helper.api.KtPackageApi
import info.papdt.express.helper.support.ResourcesUtils
import info.papdt.express.helper.support.Settings
import info.papdt.express.helper.ui.adapter.CompanyListAdapter
import info.papdt.express.helper.ui.common.AbsActivity
import info.papdt.express.helper.ui.common.SimpleRecyclerViewAdapter
import moe.feng.kotlinyan.common.*

class CompanyChooserActivity : AbsActivity() {

	private val mList: RecyclerView by lazyFindNonNullView(R.id.recycler_view)
	private val mSearchEdit: AppCompatEditText by lazy {
		AppCompatEditText(this@CompanyChooserActivity).apply {
			/** Create search edit widget  */
			setTextAppearance(this@CompanyChooserActivity, R.style.TextAppearance_AppCompat_Widget_ActionBar_Title)
			setSingleLine(true)
			setBackgroundColor(Color.TRANSPARENT)
			setHint(R.string.search_hint_company)
			imeOptions = EditorInfo.IME_ACTION_DONE
			addTextChangedListener(object : TextWatcher {
				override fun beforeTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {}
				override fun onTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {
					ui {
						val data = KtPackageApi.filterCompany(charSequence.toString())
						if (data.size == 0) {
							mEmptyView.makeVisible()
							mList.makeGone()
						} else {
							mEmptyView.makeGone()
							mList.makeVisible()
							mAdapter.setList(data)
							mAdapter.notifyDataSetChanged()
						}
					}
				}
				override fun afterTextChanged(editable: Editable) {}
			})
		}
	}
	private val mEmptyView: View by lazyFindNonNullView(R.id.empty_view)

	private lateinit var mAdapter: CompanyListAdapter
	private var data: ArrayList<Kuaidi100PackageApi.CompanyInfo.Company>? = null

	@SuppressLint("NewApi")
	override fun onCreate(savedInstanceState: Bundle?) {
		ifSupportSDK (Build.VERSION_CODES.LOLLIPOP) {
			var flag = View.SYSTEM_UI_FLAG_LAYOUT_STABLE or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !isNightMode) {
				flag = flag or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
			}
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1 && settings.getBoolean(Settings.KEY_NAVIGATION_TINT, true) && !isNightMode) {
				flag = flag or View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR
			}
			window.decorView.systemUiVisibility = flag
			window.statusBarColor = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
				Color.TRANSPARENT else resources.color[R.color.lollipop_status_bar_grey]
		}

		super.onCreate(savedInstanceState)

		if (settings.getBoolean(Settings.KEY_NAVIGATION_TINT, true)) {
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && !isNightMode) {
				window.navigationBarColor = resources.color[R.color.lollipop_status_bar_grey]
			}
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
				if (!isNightMode) {
					window.navigationBarColor = Color.WHITE
					ifSupportSDK (Build.VERSION_CODES.P) {
						window.navigationBarDividerColor = Color.argb(30, 0, 0, 0)
					}
				} else {
					window.navigationBarColor = ResourcesUtils.getColorIntFromAttr(theme, android.R.attr.windowBackground)
					ifSupportSDK (Build.VERSION_CODES.P) {
						window.navigationBarDividerColor = Color.argb(60, 255, 255, 255)
					}
				}
			}
		}

		data = Kuaidi100PackageApi.CompanyInfo.info

		setContentView(R.layout.activity_choose_company)
	}

	override fun setUpViews() {
		/** Set up custom view on ActionBar  */
		val lp = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
		mSearchEdit.layoutParams = lp

		val lp2 = ActionBar.LayoutParams(lp)
		mActionBar!!.setCustomView(mSearchEdit, lp2)

		mActionBar!!.setDisplayHomeAsUpEnabled(true)
		mActionBar!!.setDisplayShowCustomEnabled(true)
		mActionBar!!.setDisplayShowTitleEnabled(false)

		DrawableCompat.wrap(mToolbar!!.navigationIcon!!).setTint(ContextCompat.getColor(this, R.color.black_in_light))

		/** Set up company list  */
		mList.setHasFixedSize(true)
		mList.layoutManager = LinearLayoutManager(this)
		mAdapter = CompanyListAdapter(mList, data)
		mAdapter.setOnItemClickListener(object : SimpleRecyclerViewAdapter.OnItemClickListener {
			override fun onItemClick(position: Int, holder: SimpleRecyclerViewAdapter.ClickableViewHolder) {
				val intent = Intent()
				intent[RESULT_EXTRA_COMPANY_CODE] = mAdapter.getItem(position).code
				setResult(Activity.RESULT_OK, intent)
				finish()
			}
		})
		mList.adapter = mAdapter
	}

}
