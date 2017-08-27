package info.papdt.express.helper.ui.common

import android.app.Activity
import android.app.Fragment
import android.content.Context
import android.os.Bundle
import android.support.annotation.IdRes
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import info.papdt.express.helper.support.Settings
import moe.feng.kotlinyan.common.AndroidExtensions

abstract class AbsFragment : Fragment(), AndroidExtensions {

	protected var rootView: View? = null
	protected lateinit var settings: Settings
	private var mContext: Context? = null

	protected abstract fun getLayoutResId(): Int

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		settings = Settings.getInstance(context)
	}

	override fun onCreateView(inflater: LayoutInflater, parent: ViewGroup?, state: Bundle?): View? {
		if (parent != null) {
			rootView = inflater.inflate(getLayoutResId(), parent, false)
			mContext = parent.context
		} else {
			rootView = inflater.inflate(getLayoutResId(), null)
		}
		return rootView
	}

	override fun onAttach(activity: Activity) {
		super.onAttach(activity)

		// Use activity instead of getApplicationContext in order to prevent memory leak
		mContext = activity
	}

	protected fun <T : View> `$`(@IdRes viewId: Int): T? {
		return rootView?.let { it[viewId] as T }
	}

	override fun getContext(): Context? {
		return mContext
	}

}
