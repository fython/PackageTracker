package info.papdt.express.helper.ui.fragment.settings

import android.os.Bundle
import android.view.View
import android.webkit.WebView
import androidx.core.widget.NestedScrollView

import info.papdt.express.helper.R
import info.papdt.express.helper.ui.common.AbsFragment
import moe.feng.kotlinyan.common.get

class SettingsLicense : AbsFragment() {

    companion object {

        private const val ASSERT_URL = "file:///android_asset/licenses.html"

    }

	private lateinit var listContainer: NestedScrollView

	override fun getLayoutResId(): Int {
		return R.layout.fragment_license
	}

	override fun onViewCreated(rootView: View, savedInstanceState: Bundle?) {
		(rootView[R.id.web_view] as WebView).loadUrl(ASSERT_URL)

		listContainer = rootView.findViewById(android.R.id.list)
	}

}
