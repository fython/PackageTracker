package info.papdt.express.helper.ui.fragment.home

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import com.scwang.smartrefresh.header.DeliveryHeader

import com.scwang.smartrefresh.layout.SmartRefreshLayout

import com.scwang.smartrefresh.layout.api.RefreshLayout
import com.scwang.smartrefresh.layout.constant.RefreshState
import com.scwang.smartrefresh.layout.listener.OnRefreshListener
import info.papdt.express.helper.R
import info.papdt.express.helper.dao.PackageDatabase
import info.papdt.express.helper.ui.MainActivity
import info.papdt.express.helper.ui.common.AbsFragment
import info.papdt.express.helper.view.AnimatedRecyclerView
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers

abstract class BaseFragment : AbsFragment, OnRefreshListener {

	lateinit var mRefreshLayout: SmartRefreshLayout
	private lateinit var mRecyclerView: AnimatedRecyclerView
	private lateinit var mEmptyView: LinearLayout
	lateinit var mSwipeHeader: DeliveryHeader
	protected lateinit var mEmptyTips: TextView

	private var mAdapter: RecyclerView.Adapter<*>? = null

	protected var database: PackageDatabase? = null
		private set
	private var hasPlayedAnimation = false

	private var eggCount = 0
	private var bigEggCount = 0

	var isDemoRefresh = false

	constructor(database: PackageDatabase) {
		this.database = database
	}

	constructor() : super()

	// official method to get Activity Context
	override fun onAttach(context: Context?) {
		super.onAttach(context)
		sInstance = context
	}

	// restore database to reconstruct express info
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		this.database = PackageDatabase.getInstance(sInstance!!)
	}

	override fun getLayoutResId(): Int {
		return R.layout.fragment_home
	}

	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		mRefreshLayout = view.findViewById(R.id.refresh_layout)
		mRecyclerView = view.findViewById(R.id.recycler_view)
		mEmptyView = view.findViewById(R.id.empty_view)
		mSwipeHeader = view.findViewById(R.id.refresh_header)
		mEmptyTips = view.findViewById(R.id.frame_empty_tip)

		// Set up mRecyclerView
		mRecyclerView.setHasFixedSize(false)
		mRecyclerView.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)

		// Set up mRefreshLayout
		mRefreshLayout.setOnRefreshListener(this)
		mRefreshLayout.setRefreshHeader(DeliveryHeader(view.context))

		setUpAdapter()
		mEmptyView.visibility = if (mAdapter != null && mAdapter!!.itemCount > 0) View.GONE else View.VISIBLE
		mEmptyView.setOnClickListener(View.OnClickListener {
			if (eggCount++ > 7) {
				eggCount = 0
				bigEggCount++
				val eggView = `$`<View>(R.id.sun) ?: return@OnClickListener
				eggView.rotation = 0f
				eggView.animate().rotation(360f * bigEggCount).setDuration(1000).start()
				if (bigEggCount > 3) bigEggCount = 0
			}
		})
	}

	protected abstract fun setUpAdapter()
	abstract val fragmentId: Int

	override fun onRefresh(view: RefreshLayout) {
		if (isDemoRefresh) {
			isDemoRefresh = false
			return
		}
		mHandler.sendEmptyMessage(FLAG_REFRESH_LIST)
	}

	fun notifyDataSetChanged() {
		mHandler.sendEmptyMessage(FLAG_UPDATE_ADAPTER_ONLY)
	}

	fun scrollToTop() {
		if (mAdapter != null && mAdapter!!.itemCount > 0) {
			mRecyclerView.smoothScrollToPosition(0)
		}
	}

	protected fun playListAnimation() {
		if (!hasPlayedAnimation) {
			hasPlayedAnimation = true
			mRecyclerView.scheduleLayoutAnimation()
		}
	}

	protected fun setAdapter(adapter: RecyclerView.Adapter<*>) {
		this.mAdapter = adapter
		mRecyclerView.adapter = mAdapter
		mEmptyView.visibility = if (mAdapter != null && mAdapter!!.itemCount > 0) View.GONE else View.VISIBLE
	}

	protected val mainActivity: MainActivity
		get() = activity as MainActivity

	fun onUndoActionClicked() {
		val position = database!!.undoLastRemoval()
		if (position >= 0 && mAdapter != null) {
			mAdapter!!.notifyDataSetChanged()
			mainActivity.notifyDataChanged(fragmentId)
		}
	}

	private val mHandler = @SuppressLint("HandlerLeak")
	object : Handler() {
		override fun handleMessage(msg: Message) {
			when (msg.what) {
				FLAG_REFRESH_LIST -> {
					if (mRefreshLayout.state != RefreshState.Refreshing) {
						mRefreshLayout.autoRefresh()
					}
					Observable.just(false).map(database!!::pullDataFromNetwork)
							.subscribeOn(Schedulers.io())
							.observeOn(AndroidSchedulers.mainThread())
							.subscribe {
								hasPlayedAnimation = false
								mRefreshLayout.finishRefresh()
								sendEmptyMessage(FLAG_UPDATE_ADAPTER_ONLY)
							}
				}
				FLAG_UPDATE_ADAPTER_ONLY -> if (mAdapter != null) {
					mAdapter!!.notifyDataSetChanged()
					playListAnimation()
					mEmptyView.visibility = if (mAdapter != null && mAdapter!!.itemCount > 0) View.GONE else View.VISIBLE
				}
			}
		}
	}

	companion object {

		@SuppressLint("StaticFieldLeak")
		private var sInstance: Context? = null
		private const val FLAG_REFRESH_LIST = 0
		private const val FLAG_UPDATE_ADAPTER_ONLY = 1

	}

}
