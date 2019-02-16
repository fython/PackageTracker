package info.papdt.express.helper.ui.fragment.settings

import android.os.Bundle
import android.view.View
import android.webkit.WebView
import info.papdt.express.helper.Licenses

import info.papdt.express.helper.R
import info.papdt.express.helper.ui.common.AbsFragment
import moe.feng.kotlinyan.common.get

class SettingsLicense : AbsFragment() {

	override fun getLayoutResId(): Int {
		return R.layout.fragment_license
	}

	override fun onViewCreated(rootView: View, savedInstanceState: Bundle?) {
		val webView = rootView[R.id.web_view] as WebView
		webView.loadData(Licenses.html, "text/html", null)
	}

}
