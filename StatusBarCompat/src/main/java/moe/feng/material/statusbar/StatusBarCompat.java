package moe.feng.material.statusbar;

import android.app.Activity;
import android.graphics.Color;
import android.os.Build;
import android.view.View;

import moe.feng.material.statusbar.util.ViewHelper;

public class StatusBarCompat {

	public static void setUpActivity(Activity activity) {
		if (Build.VERSION.SDK_INT >= 19 && !ViewHelper.isChrome()) {
			activity.getWindow().getDecorView()
					.setSystemUiVisibility(
							View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
					);
		}

		if (Build.VERSION.SDK_INT >= 21) {
			activity.getWindow().setStatusBarColor(Color.TRANSPARENT);
		}
	}

}
