package info.papdt.express.helper.view

import android.content.Context
import android.graphics.*
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.support.annotation.ColorInt
import android.support.annotation.ColorRes
import android.support.annotation.DrawableRes
import android.support.v4.content.ContextCompat
import android.util.AttributeSet
import android.view.View
import info.papdt.express.helper.R
import info.papdt.express.helper.support.ScreenUtils
import moe.feng.kotlinyan.common.*

class VerticalStepIconView @JvmOverloads
constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) : View(context, attrs, defStyleAttr) {

	private var mPaint: Paint = Paint(Paint.ANTI_ALIAS_FLAG)
	private var mCirclePaint: Paint = Paint(Paint.ANTI_ALIAS_FLAG)
	private var mBounds: RectF? = null
	private var mIconBounds: RectF? = null
	private var radius: Float = 0.toFloat()
	private val lineWidth: Float
	private var pointOffsetY = 0f
	private val iconSize: Float

	@ColorInt private var pointColor = Color.BLUE
	@ColorInt private val iconColor = Color.WHITE
	private var isMini = false
	private var centerIcon: Drawable? = null
	private var centerIconBitmap: Bitmap? = null

	init {
		lineWidth = ScreenUtils.dpToPx(context, 2f)
		iconSize = ScreenUtils.dpToPx(context, 16f)

		init()

		pointColor = ContextCompat.getColor(context, R.color.blue_500)
	}

	internal fun init() {
		mPaint.style = Paint.Style.FILL_AND_STROKE
		mPaint.strokeWidth = lineWidth

		mCirclePaint.style = Paint.Style.FILL_AND_STROKE
		mCirclePaint.strokeWidth = lineWidth
		mCirclePaint.color = pointColor
	}

	fun setPointColor(@ColorInt color: Int) {
		pointColor = color
	}

	fun setPointColorResource(@ColorRes resId: Int) {
		pointColor = ContextCompat.getColor(context, resId)
	}

	fun setIsMini(isMini: Boolean) {
		this.isMini = isMini
	}

	fun setCenterIcon(drawable: Drawable?) {
		this.centerIcon = drawable
		this.centerIconBitmap = applyBitmapFromDrawable(drawable)
	}

	fun setCenterIcon(@DrawableRes resId: Int) {
		setCenterIcon(ContextCompat.getDrawable(context, resId))
	}

	fun setPointOffsetY(pointOffsetY: Float) {
		this.pointOffsetY = pointOffsetY
	}

	override fun onSizeChanged(w: Int, h: Int, oldW: Int, oldH: Int) {
		super.onSizeChanged(w, h, oldW, oldH)
		mBounds = RectF(left.toFloat(), top.toFloat(), right.toFloat(), bottom.toFloat())
		mIconBounds = RectF(mBounds!!.centerX() - iconSize / 2, mBounds!!.centerY() - iconSize / 2 + pointOffsetY,
				mBounds!!.centerX() + iconSize / 2, mBounds!!.centerY() + iconSize / 2 + pointOffsetY)
		radius = (Math.min(w, h) / 4).toFloat()
	}

	override fun onDraw(canvas: Canvas) {
		val r = if (isMini) radius / 5 * 3 else radius

		super.onDraw(canvas)

		mCirclePaint.color = pointColor
		canvas.drawCircle(mBounds!!.centerX(), mBounds!!.centerY() + pointOffsetY, r, mCirclePaint)

		if (centerIcon != null) {
			mPaint.color = iconColor
			mIconBounds!!.top = mBounds!!.centerY() - iconSize / 2 + pointOffsetY
			mIconBounds!!.bottom = mBounds!!.centerY() + iconSize / 2 + pointOffsetY
			canvas.drawBitmap(centerIconBitmap!!, null, mIconBounds!!, mPaint)
		}
	}

	private fun applyBitmapFromDrawable(d: Drawable?): Bitmap? {
		if (d == null) {
			return null
		}

		if (d is BitmapDrawable) {
			return d.bitmap
		}

		return try {
			val bitmap: Bitmap = if (d is ColorDrawable) {
				Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888)
			} else {
				Bitmap.createBitmap(
						Math.max(24f.dpToPx(context).toInt(), d.intrinsicWidth),
						Math.max(24f.dpToPx(context).toInt(), d.intrinsicHeight),
						Bitmap.Config.ARGB_8888
				)
			}

			val canvas = Canvas(bitmap)
			d.setBounds(0, 0, canvas.width, canvas.height)
			d.draw(canvas)
			bitmap
		} catch (e: OutOfMemoryError) {
			null
		}
	}

}
