package info.papdt.express.helper.ui.fragment.settings

import android.os.Bundle
import android.view.View
import android.webkit.WebView

import info.papdt.express.helper.R
import info.papdt.express.helper.ui.common.AbsFragment

class SettingsLicense : AbsFragment() {

	private val ASSERT_URL = "file:///android_asset/licenses.html"

	override fun getLayoutResId(): Int {
		return R.layout.fragment_license
	}

	override fun onViewCreated(rootView: View, savedInstanceState: Bundle?) {
		(rootView[R.id.web_view] as WebView).loadUrl(ASSERT_URL)
	}

}
