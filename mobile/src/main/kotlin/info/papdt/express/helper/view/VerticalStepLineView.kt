package info.papdt.express.helper.view

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import androidx.annotation.ColorInt
import androidx.annotation.ColorRes
import androidx.core.content.ContextCompat
import android.util.AttributeSet
import android.view.View

import info.papdt.express.helper.R
import info.papdt.express.helper.support.ScreenUtils

class VerticalStepLineView @JvmOverloads
constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) : View(context, attrs, defStyleAttr) {

	private var mPaint: Paint =  Paint(Paint.ANTI_ALIAS_FLAG)
	private var mBounds: RectF? = null
	private val lineWidth: Float
	private var pointOffsetY = 0f

	@ColorInt private var lineColor = Color.GRAY
	private var shouldDrawTopLine = true
	private var shouldDrawBottomLine = true

	init {
		lineWidth = ScreenUtils.dpToPx(context, 2f)

		init()

		lineColor = ContextCompat.getColor(context, R.color.blue_grey_500)
	}

	private fun init() {
		mPaint.style = Paint.Style.FILL_AND_STROKE
		mPaint.strokeWidth = lineWidth
	}

	fun setLineShouldDraw(top: Boolean, bottom: Boolean) {
		shouldDrawTopLine = top
		shouldDrawBottomLine = bottom
	}

	fun setLineColor(@ColorInt color: Int) {
		lineColor = color
	}

	fun setLineColorResource(@ColorRes resId: Int) {
		lineColor = ContextCompat.getColor(context, resId)
	}

	fun setPointOffsetY(pointOffsetY: Float) {
		this.pointOffsetY = pointOffsetY
	}

	override fun onSizeChanged(w: Int, h: Int, oldW: Int, oldH: Int) {
		super.onSizeChanged(w, h, oldW, oldH)
		mBounds = RectF(left.toFloat(), top.toFloat(), right.toFloat(), bottom.toFloat())
	}

	override fun onDraw(canvas: Canvas) {
		super.onDraw(canvas)

		if (shouldDrawTopLine) {
			mPaint.color = lineColor
			mBounds?.run { canvas.drawLine(centerX(), centerY() + pointOffsetY, centerX(), top, mPaint) }
		}

		if (shouldDrawBottomLine) {
			mPaint.color = lineColor
			mBounds?.run { canvas.drawLine(centerX(), centerY() + pointOffsetY, centerX(), bottom, mPaint) }
		}
	}

}
