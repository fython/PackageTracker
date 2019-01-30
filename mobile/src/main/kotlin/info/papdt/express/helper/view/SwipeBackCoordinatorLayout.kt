package info.papdt.express.helper.view

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.Animation
import android.view.animation.Transformation

import androidx.annotation.ColorInt
import androidx.annotation.IntDef
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.view.ViewCompat

/**
 * Swipe back coordinator layout.
 *
 * A [CoordinatorLayout] that has swipe back operation.
 *
 */

class SwipeBackCoordinatorLayout : CoordinatorLayout {

    companion object {

        private const val SWIPE_RADIO = 0.33f

        const val NULL_DIR = 0
        const val UP_DIR = 1
        const val DOWN_DIR = -1

        /**
         * Whether the SwipeBackView can swipe back.
         *
         * @param v   child view.
         * @param dir drag direction.
         */
        fun canSwipeBack(v: View, dir: Int): Boolean {
            return !v.canScrollVertically(dir)
        }

        /**
         * Compute shadow background alpha by drag percent.
         *
         * @param percent drag percent.
         *
         * @return Color.
         */
        fun getBackgroundAlpha(percent: Float): Float {
            return (0.9 - percent * (0.9 - 0.5)).toFloat()
        }

        /**
         * Compute shadow background color by drag percent.
         *
         * @param percent drag percent.
         *
         * @return Color.
         */
        @ColorInt
        fun getBackgroundColor(percent: Float): Int {
            return Color.argb((255 * getBackgroundAlpha(percent)).toInt(), 0, 0, 0)
        }

        /**
         * Execute alpha animation to hide background.
         *
         * @param background The view to show shadow background.
         */

        fun hideBackgroundWithAlphaAnim(background: View) {
            val a = ResetAlphaAnimation(background, false)
            a.duration = 200
            background.startAnimation(a)
        }

        /**
         * Execute color animation to hide background.
         *
         * @param background The view to show shadow background.
         */
        fun hideBackgroundWithColorAnim(background: View) {
            val a = RecolorAnimation(background, false)
            a.duration = 200
            background.startAnimation(a)
        }

    }

    @IntDef(NULL_DIR, UP_DIR, DOWN_DIR)
    annotation class DirectionRule

    private var swipeListener: OnSwipeListener? = null

    private var swipeDistance: Int = 0
    private var swipeTrigger: Float = 0.toFloat()

    private var isVerticalDragged: Boolean = false

    @DirectionRule
    private var swipeDir = NULL_DIR

    private val resetAnimListener = object : Animation.AnimationListener {

        override fun onAnimationStart(animation: Animation) {
            isEnabled = false
        }

        override fun onAnimationEnd(animation: Animation) {
            isEnabled = true
        }

        override fun onAnimationRepeat(animation: Animation) {
            // do nothing.
        }
    }

    private inner class ResetAnimation internal constructor(private val fromDistance: Int) : Animation() {

        public override fun applyTransformation(interpolatedTime: Float, t: Transformation) {
            swipeDistance = (fromDistance * (1 - interpolatedTime)).toInt()
            setSwipeTranslation()
        }
    }

    private class ResetAlphaAnimation internal constructor(private val view: View, private val showing: Boolean) : Animation() {

        override fun applyTransformation(interpolatedTime: Float, t: Transformation) {
            super.applyTransformation(interpolatedTime, t)
            if (showing) {
                view.alpha = (0.5 * interpolatedTime).toFloat()
            } else {
                view.alpha = (0.5 * (1 - interpolatedTime)).toFloat()
            }
        }
    }

    private class RecolorAnimation internal constructor(private val view: View, private val showing: Boolean) : Animation() {

        override fun applyTransformation(interpolatedTime: Float, t: Transformation) {
            super.applyTransformation(interpolatedTime, t)
            if (showing) {
                view.setBackgroundColor(Color.argb((255.0 * 0.5 * interpolatedTime.toDouble()).toInt(), 0, 0, 0))
            } else {
                view.setBackgroundColor(Color.argb((255.0 * 0.5 * (1 - interpolatedTime).toDouble()).toInt(), 0, 0, 0))
            }
        }
    }

    constructor(context: Context) : super(context) {
        this.initialize()
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        this.initialize()
    }

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        this.initialize()
    }

    private fun initialize() {
        this.swipeDistance = 0
        this.swipeTrigger = (resources.displayMetrics.heightPixels / 4.0).toFloat()
    }

    // nested scroll.

    override fun onStartNestedScroll(child: View, target: View, nestedScrollAxes: Int, type: Int): Boolean {
        super.onStartNestedScroll(child, target, nestedScrollAxes, type)
        isVerticalDragged = nestedScrollAxes and ViewCompat.SCROLL_AXIS_VERTICAL != 0
        return type == 0
    }

    override fun onNestedPreScroll(target: View, dx: Int, dy: Int, consumed: IntArray, type: Int) {
        var dyConsumed = 0
        if (isVerticalDragged && swipeDistance != 0) {
            dyConsumed = onVerticalPreScroll(dy)
        }

        val newConsumed = intArrayOf(0, 0)
        super.onNestedPreScroll(target, dx, dy - dyConsumed, newConsumed, type)

        consumed[0] = newConsumed[0]
        consumed[1] = newConsumed[1] + dyConsumed
    }

    override fun onNestedScroll(target: View, dxConsumed: Int, dyConsumed: Int,
                                dxUnconsumed: Int, dyUnconsumed: Int, type: Int) {
        var newDyConsumed = dyConsumed
        var newDyUnconsumed = dyUnconsumed
        if (isVerticalDragged && swipeDistance == 0) {
            val dir = if (dyUnconsumed < 0) DOWN_DIR else UP_DIR
            if (swipeListener != null && swipeListener!!.canSwipeBack(dir)) {
                onVerticalScroll(dyUnconsumed)
                newDyConsumed = dyConsumed + dyUnconsumed
                newDyUnconsumed = 0
            }
        }

        super.onNestedScroll(target, dxConsumed, newDyConsumed, dxUnconsumed, newDyUnconsumed, type)
    }

    override fun onStopNestedScroll(child: View, type: Int) {
        super.onStopNestedScroll(child, type)
        if (isVerticalDragged) {
            if (Math.abs(swipeDistance) >= swipeTrigger) {
                swipeBack()
            } else {
                reset()
            }
        }
    }

    private fun onVerticalPreScroll(dy: Int): Int {
        val consumed: Int
        if (swipeDistance * (swipeDistance - dy) < 0) {
            swipeDir = NULL_DIR
            consumed = swipeDistance
            swipeDistance = 0
        } else {
            consumed = dy
            swipeDistance -= dy
        }

        setSwipeTranslation()

        return consumed
    }

    private fun onVerticalScroll(dy: Int) {
        swipeDistance = -dy
        swipeDir = if (swipeDistance > 0) DOWN_DIR else UP_DIR

        setSwipeTranslation()
    }

    private fun swipeBack() {
        if (swipeListener != null) {
            swipeListener!!.onSwipeFinish(swipeDir)
        }
    }

    fun reset() {
        swipeDir = NULL_DIR
        if (swipeDistance != 0) {
            val a = ResetAnimation(swipeDistance)
            a.duration = (200.0 + 100.0 * Math.abs(swipeDistance) / swipeTrigger).toLong()
            a.interpolator = AccelerateDecelerateInterpolator()
            a.setAnimationListener(resetAnimListener)
            startAnimation(a)
        }
    }

    private fun setSwipeTranslation() {
        val dir = if (swipeDistance > 0) UP_DIR else DOWN_DIR
        translationY = (dir.toDouble() * SWIPE_RADIO.toDouble() * swipeTrigger.toDouble()
                * Math.log10(1 + 9.0 * Math.abs(swipeDistance) / swipeTrigger)).toFloat()
        if (swipeListener != null) {
            swipeListener!!.onSwipeProcess(
                    Math.min(
                            1.0,
                            Math.abs(1.0 * swipeDistance / swipeTrigger)).toFloat())
        }
    }

    // interface.

    // on swipe listener.

    interface OnSwipeListener {
        fun canSwipeBack(dir: Int): Boolean
        fun onSwipeProcess(percent: Float)
        fun onSwipeFinish(dir: Int)
    }

    fun setOnSwipeListener(l: OnSwipeListener) {
        this.swipeListener = l
    }

}