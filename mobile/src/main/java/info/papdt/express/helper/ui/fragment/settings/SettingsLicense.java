package info.papdt.express.helper.ui.fragment.settings;

import android.view.View;
import android.webkit.WebView;

import info.papdt.express.helper.R;
import info.papdt.express.helper.ui.common.AbsFragment;

public class SettingsLicense extends AbsFragment {

	private final static String ASSERT_URL = "file:///android_asset/licenses.html";

	@Override
	protected int getLayoutResId() {
		return R.layout.fragment_license;
	}

	@Override
	protected void doCreateView(View rootView) {
		((WebView) $(R.id.web_view)).loadUrl(ASSERT_URL);
	}

}
