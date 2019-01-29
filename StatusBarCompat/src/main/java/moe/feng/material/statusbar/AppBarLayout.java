package moe.feng.material.statusbar;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.os.Build;
import androidx.annotation.ColorInt;
import androidx.annotation.ColorRes;
import android.util.AttributeSet;
import android.widget.LinearLayout;

import moe.feng.material.statusbar.util.ViewHelper;

public class AppBarLayout extends LinearLayout {

	private int colorNormal, colorDark, enableMode;

	private StatusBarHeaderView headerView;

	public static final int MODE_KITKAT = 1, MODE_LOLLIPOP = 2, MODE_ALL = 3;

	public AppBarLayout(Context context) {
		this(context, null);
	}

	public AppBarLayout(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public AppBarLayout(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.StatusBarHeaderView, defStyle,
				R.style.Widget_FengMoe_StatusBarHeaderView);
		colorNormal = a.getColor(R.styleable.StatusBarHeaderView_colorNormal, Color.TRANSPARENT);
		if (a.hasValue(R.styleable.StatusBarHeaderView_colorDark)) {
			colorDark = a.getColor(R.styleable.StatusBarHeaderView_colorDark, Color.TRANSPARENT);
		} else {
			colorDark = ViewHelper.getMiddleColor(colorNormal, Color.BLACK, 0.2f);
		}
		enableMode = a.getInt(R.styleable.StatusBarHeaderView_enableMode, MODE_ALL);
		headerView = new StatusBarHeaderView(context, colorNormal, colorDark, enableMode);
		this.setBackgroundColor(colorNormal);
		this.setOrientation(LinearLayout.VERTICAL);
		this.addView(headerView);
		a.recycle();
		if (Build.VERSION.SDK_INT >= 21) {
			this.setElevation(ViewHelper.dpToPx(context, 5f));
		}
	}

	public void setNormalColor(@ColorInt int colorNormal) {
		this.colorNormal = colorNormal;
		this.setBackgroundColor(colorNormal);
		headerView.setNormalColor(colorNormal);
		headerView.init();
	}

	public void setDarkColor(@ColorInt int colorDark) {
		this.colorDark = colorDark;
		headerView.setDarkColor(colorDark);
		headerView.init();
	}

	public void setColor(@ColorInt int colorNormal,@ColorInt int colorDark) {
		this.colorNormal = colorNormal;
		this.colorDark = colorDark;
		this.setBackgroundColor(colorNormal);
		headerView.setNormalColor(colorNormal);
		headerView.setDarkColor(colorDark);
		headerView.init();
	}

	public void setColorResources(@ColorRes int colorNormal, @ColorRes int colorDark) {
		this.setColor(
				getResources().getColor(colorNormal),
				getResources().getColor(colorDark)
		);
	}

	public int getNormalColor() {
		return this.colorNormal;
	}

	public int getDarkColor(){
		return this.colorDark;
	}

	public void setMode(int mode) {
		this.enableMode = mode;
		headerView.setMode(mode);
		headerView.init();
	}

	public int getMode(){
		return this.enableMode;
	}

}
