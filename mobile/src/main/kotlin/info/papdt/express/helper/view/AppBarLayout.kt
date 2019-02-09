package info.papdt.express.helper.view

import android.content.Context
import android.graphics.Color
import android.os.Build
import androidx.annotation.ColorInt
import androidx.annotation.ColorRes
import android.util.AttributeSet
import android.widget.LinearLayout

import info.papdt.express.helper.R
import info.papdt.express.helper.support.ScreenUtils

class AppBarLayout
@JvmOverloads
constructor(context: Context, attrs: AttributeSet? = null, defStyle: Int = 0)
    : LinearLayout(context, attrs, defStyle) {

    companion object {

        const val MODE_KITKAT = 1
        const val MODE_LOLLIPOP = 2
        const val MODE_ALL = 3

    }

    private var colorNormal: Int = 0
    private var colorDark: Int = 0
    private var enableMode: Int = 0

    private val headerView: StatusBarHeaderView

    var normalColor: Int
        get() = this.colorNormal
        set(@ColorInt colorNormal) {
            this.colorNormal = colorNormal
            this.setBackgroundColor(colorNormal)
            headerView.normalColor = colorNormal
            headerView.init()
        }

    var darkColor: Int
        get() = this.colorDark
        set(@ColorInt colorDark) {
            this.colorDark = colorDark
            headerView.darkColor = colorDark
            headerView.init()
        }

    var mode: Int
        get() = this.enableMode
        set(mode) {
            this.enableMode = mode
            headerView.mode = mode
            headerView.init()
        }

    init {
        val a = context.obtainStyledAttributes(attrs, R.styleable.StatusBarHeaderView, defStyle,
                R.style.Widget_FengMoe_StatusBarHeaderView)
        colorNormal = a.getColor(R.styleable.StatusBarHeaderView_colorNormal, Color.TRANSPARENT)
        if (a.hasValue(R.styleable.StatusBarHeaderView_colorDark)) {
            colorDark = a.getColor(R.styleable.StatusBarHeaderView_colorDark, Color.TRANSPARENT)
        } else {
            colorDark = ScreenUtils.getMiddleColor(colorNormal, Color.BLACK, 0.2f)
        }
        enableMode = a.getInt(R.styleable.StatusBarHeaderView_enableMode, MODE_ALL)
        headerView = StatusBarHeaderView(context, colorNormal, colorDark, enableMode)
        this.setBackgroundColor(colorNormal)
        this.orientation = LinearLayout.VERTICAL
        this.addView(headerView)
        a.recycle()
        if (Build.VERSION.SDK_INT >= 21) {
            this.elevation = ScreenUtils.dpToPx(context, 6f)
        }
    }

    fun setColor(@ColorInt colorNormal: Int, @ColorInt colorDark: Int) {
        this.colorNormal = colorNormal
        this.colorDark = colorDark
        this.setBackgroundColor(colorNormal)
        headerView.normalColor = colorNormal
        headerView.darkColor = colorDark
        headerView.init()
    }

    fun setColorResources(@ColorRes colorNormal: Int, @ColorRes colorDark: Int) {
        this.setColor(
                resources.getColor(colorNormal),
                resources.getColor(colorDark)
        )
    }

}
