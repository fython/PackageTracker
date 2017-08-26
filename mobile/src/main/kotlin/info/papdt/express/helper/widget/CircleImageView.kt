/*
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package info.papdt.express.helper.widget

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RadialGradient
import android.graphics.Shader
import android.graphics.drawable.ShapeDrawable
import android.graphics.drawable.shapes.OvalShape
import android.support.v4.view.ViewCompat
import android.view.animation.Animation
import android.widget.ImageView

/**
 * Private class created to work around issues with AnimationListeners being
 * called before the animation is actually complete and support shadows on older
 * platforms.
 *
 * @hide
 */
internal class CircleImageView(context: Context, color: Int, radius: Float) : ImageView(context) {

	private var mListener: Animation.AnimationListener? = null
	private var mShadowRadius: Int = 0

	init {
		val density = getContext().resources.displayMetrics.density
		val diameter = (radius * density * 2f).toInt()
		val shadowYOffset = (density * Y_OFFSET).toInt()
		val shadowXOffset = (density * X_OFFSET).toInt()

		mShadowRadius = (density * SHADOW_RADIUS).toInt()

		val circle: ShapeDrawable
		if (elevationSupported()) {
			circle = ShapeDrawable(OvalShape())
			ViewCompat.setElevation(this, SHADOW_ELEVATION * density)
		} else {
			val oval = OvalShadow(mShadowRadius, diameter)
			circle = ShapeDrawable(oval)
			ViewCompat.setLayerType(this, ViewCompat.LAYER_TYPE_SOFTWARE, circle.paint)
			circle.paint.setShadowLayer(mShadowRadius.toFloat(), shadowXOffset.toFloat(), shadowYOffset.toFloat(),
					KEY_SHADOW_COLOR)
			val padding = mShadowRadius
			// set padding so the inner image sits correctly within the shadow.
			setPadding(padding, padding, padding, padding)
		}
		circle.paint.color = color
		setBackgroundDrawable(circle)
	}

	private fun elevationSupported(): Boolean {
		return android.os.Build.VERSION.SDK_INT >= 21
	}

	override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
		super.onMeasure(widthMeasureSpec, heightMeasureSpec)
		if (!elevationSupported()) {
			setMeasuredDimension(measuredWidth + mShadowRadius * 2, measuredHeight + mShadowRadius * 2)
		}
	}

	fun setAnimationListener(listener: Animation.AnimationListener?) {
		mListener = listener
	}

	public override fun onAnimationStart() {
		super.onAnimationStart()
		if (mListener != null) {
			mListener!!.onAnimationStart(animation)
		}
	}

	public override fun onAnimationEnd() {
		super.onAnimationEnd()
		mListener?.onAnimationEnd(animation)
	}

	/**
	 * Update the background color of the circle image view.
	 *
	 * @param colorRes Id of a color resource.
	 */
	fun setBackgroundColorRes(colorRes: Int) {
		setBackgroundColor(context.resources.getColor(colorRes))
	}

	override fun setBackgroundColor(color: Int) {
		if (background is ShapeDrawable) {
			(background as ShapeDrawable).paint.color = color
		}
	}

	private inner class OvalShadow(shadowRadius: Int, private val mCircleDiameter: Int) : OvalShape() {
		private val mRadialGradient: RadialGradient
		private val mShadowPaint: Paint = Paint()

		init {
			mShadowRadius = shadowRadius
			mRadialGradient = RadialGradient((mCircleDiameter / 2).toFloat(), (mCircleDiameter / 2).toFloat(),
					mShadowRadius.toFloat(), intArrayOf(FILL_SHADOW_COLOR, Color.TRANSPARENT), null, Shader.TileMode.CLAMP)
			mShadowPaint.shader = mRadialGradient
		}

		override fun draw(canvas: Canvas, paint: Paint) {
			val viewWidth = this@CircleImageView.width
			val viewHeight = this@CircleImageView.height
			canvas.drawCircle((viewWidth / 2).toFloat(), (viewHeight / 2).toFloat(), (mCircleDiameter / 2 + mShadowRadius).toFloat(),
					mShadowPaint)
			canvas.drawCircle((viewWidth / 2).toFloat(), (viewHeight / 2).toFloat(), (mCircleDiameter / 2).toFloat(), paint)
		}
	}

	companion object {

		private val KEY_SHADOW_COLOR = 0x1E000000
		private val FILL_SHADOW_COLOR = 0x3D000000
		// PX
		private val X_OFFSET = 0f
		private val Y_OFFSET = 1.75f
		private val SHADOW_RADIUS = 3.5f
		private val SHADOW_ELEVATION = 4

	}

}