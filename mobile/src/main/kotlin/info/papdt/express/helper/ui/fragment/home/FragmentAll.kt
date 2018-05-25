package info.papdt.express.helper.ui.fragment.home

import android.annotation.SuppressLint
import android.os.Bundle
import info.papdt.express.helper.R

import info.papdt.express.helper.dao.PackageDatabase
import info.papdt.express.helper.ui.adapter.HomePackageListAdapter

@SuppressLint("ValidFragment")
class FragmentAll : BaseFragment {

	constructor(database: PackageDatabase) : super(database)

	constructor() : super()

	override fun setUpAdapter() {
		val adapter = HomePackageListAdapter(database, arguments!!.getInt(ARG_TYPE), mainActivity)
		setAdapter(adapter)
		when (arguments!!.getInt(ARG_TYPE)) {
			TYPE_ALL -> mEmptyTips.setText(R.string.frame_empty_tip_all)
			TYPE_DELIVERED -> mEmptyTips.setText(R.string.frame_empty_tip_delivered)
			TYPE_DELIVERING -> mEmptyTips.setText(R.string.frame_empty_tip_delivering)
		}
	}

	override val fragmentId: Int
		get() = arguments!!.getInt(ARG_TYPE)

	companion object {

		private val ARG_TYPE = "arg_type"

		const val TYPE_ALL = 0
		const val TYPE_DELIVERED = 1
		const val TYPE_DELIVERING = 2

		fun newInstance(db: PackageDatabase, type: Int): FragmentAll {
			val fragment = FragmentAll(db)
			val data = Bundle()
			data.putInt(ARG_TYPE, type)
			fragment.arguments = data
			return fragment
		}

	}

}
