package info.papdt.express.helper.view

import android.content.Context
import android.graphics.Color
import android.os.Build
import android.util.AttributeSet
import android.view.View

import info.papdt.express.helper.R
import info.papdt.express.helper.support.ScreenUtils

class StatusBarHeaderView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyle: Int = 0) : View(context, attrs, defStyle) {

    private var colorNormal: Int = 0
    private var colorDark: Int = 0
    private var enableMode: Int = 0

    var normalColor: Int
        get() = this.colorNormal
        set(colorNormal) {
            this.colorNormal = colorNormal
            init()
        }

    var darkColor: Int
        get() = this.colorDark
        set(colorDark) {
            this.colorDark = colorDark
            init()
        }

    var mode: Int
        get() = this.enableMode
        set(mode) {
            this.enableMode = mode
            init()
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
        init()
        a.recycle()
    }

    constructor(context: Context, colorNormal: Int, colorDark: Int, enableMode: Int) : this(context) {
        this.colorNormal = colorNormal
        this.colorDark = colorDark
        this.enableMode = enableMode
        init()
    }

    public override fun onMeasure(widthSpec: Int, heightSpec: Int) {
        super.onMeasure(widthSpec, heightSpec)
        adjustHeight()
    }

    override fun invalidate() {
        super.invalidate()
        adjustHeight()
    }

    fun adjustHeight() {
        val params = layoutParams
        params.height = ScreenUtils.getStatusBarHeight(context)
    }

    internal fun init() {
        val SDK_INT = Build.VERSION.SDK_INT
        this.setBackgroundColor(if (SDK_INT == 19) colorNormal else colorDark)
        this.visibility = if (!ScreenUtils.isChrome() &&
                (enableMode == MODE_KITKAT && SDK_INT == 19 ||
                        enableMode == MODE_LOLLIPOP && SDK_INT >= 21 ||
                        enableMode == MODE_ALL && SDK_INT >= 19))
            View.VISIBLE
        else
            View.GONE
    }

    companion object {

        val MODE_KITKAT = 1
        val MODE_LOLLIPOP = 2
        val MODE_ALL = 3
    }

}
