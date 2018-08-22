package info.papdt.express.helper.ui

import android.annotation.SuppressLint
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.support.v7.widget.AppCompatTextView
import android.view.View
import android.widget.ImageView

import info.papdt.express.helper.R
import info.papdt.express.helper.support.MaterialImageLoader
import info.papdt.express.helper.support.ResourcesUtils
import info.papdt.express.helper.ui.common.AbsActivity
import moe.feng.kotlinyan.common.*

class SplashActivity : AbsActivity() {

	private val mLogoView: ImageView by lazyFindNonNullView(R.id.iv_logo)
	private val mTitleView: AppCompatTextView by lazyFindNonNullView(R.id.tv_title)

	@SuppressLint("NewApi")
	override fun onCreate(savedInstanceState: Bundle?) {
		val STATUS_BAR_COLOR = resources.color[R.color.lollipop_status_bar_grey]
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
			var flag = View.SYSTEM_UI_FLAG_LAYOUT_STABLE or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
			if (!isNightMode) {
				if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
					flag = flag or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
				}
				if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
					flag = flag or View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR
				}
			}
			window.decorView.systemUiVisibility = flag
			window.statusBarColor = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) Color.TRANSPARENT else STATUS_BAR_COLOR
			if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O_MR1) {
				window.navigationBarColor = STATUS_BAR_COLOR
			} else if (!isNightMode) {
				window.navigationBarColor = Color.WHITE
				window.navigationBarDividerColor = Color.argb(30, 0, 0, 0)
			} else {
				window.navigationBarColor = ResourcesUtils.getColorIntFromAttr(theme, android.R.attr.windowBackground)
				window.navigationBarDividerColor = Color.argb(60, 255, 255, 255)
			}
		}

		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_splash_screen)

		Handler().postDelayed({
			MainActivity.launch(this@SplashActivity)
			finish()
		}, 3000)
	}

	override fun setUpViews() {
		MaterialImageLoader.animate(mLogoView).setDuration(1000).start()
		mTitleView.animate().alpha(1f).setDuration(500).start()
	}

}
