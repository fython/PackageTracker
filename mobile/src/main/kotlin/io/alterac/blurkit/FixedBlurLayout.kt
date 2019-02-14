package io.alterac.blurkit

import android.app.Activity
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Matrix
import android.graphics.Point
import android.graphics.PointF
import android.graphics.Rect
import android.util.AttributeSet
import android.view.Choreographer
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView

import java.lang.ref.WeakReference

/**
 * A [ViewGroup] that blurs all content behind it. Automatically creates bitmap of parent content
 * and finds its relative position to the top parent to draw properly regardless of where the layout is
 * placed.
 */
class FixedBlurLayout : FrameLayout {

    companion object {

        const val DEFAULT_DOWNSCALE_FACTOR = 0.12f
        const val DEFAULT_BLUR_RADIUS = 12
        const val DEFAULT_FPS = 60
        const val DEFAULT_CORNER_RADIUS = 0f

    }

    // Customizable attributes

    /** Factor to scale the view bitmap with before blurring.  */
    private var mDownscaleFactor: Float = 0F

    /** Blur radius passed directly to stackblur library.  */
    private var mBlurRadius: Int = 0

    /** Number of blur invalidations to do per second.   */
    private var mFPS: Int = 0

    /** Corner radius for the layouts blur. To make rounded rects and circles.  */
    private var mCornerRadius: Float = 0F

    /** Is blur running?  */
    private var mRunning: Boolean = false

    /** Is window attached?  */
    private var mAttachedToWindow: Boolean = false

    /** Do we need to recalculate the position each invalidation?  */
    /**
     * Get the locked position value.
     * See [.mPositionLocked].
     */
    var positionLocked: Boolean = false
        private set

    /** Do we need to regenerate the view bitmap each invalidation?  */
    /**
     * Get the view locked value.
     * See [.mViewLocked].
     */
    var viewLocked: Boolean = false
        private set

    // Calculated class dependencies

    /** ImageView to show the blurred content.  */
    private lateinit var mImageView: RoundedImageView

    /** Reference to View for top-parent. For retrieval see [getActivityView][.getActivityView].  */
    private var mActivityView: WeakReference<View>? = null

    /** A saved point to re-use when [.lockPosition] called.  */
    private var mLockedPoint: Point? = null

    /** A saved bitmap for the view to re-use when [.lockView] called.  */
    private var mLockedBitmap: Bitmap? = null

    /** A saved alpha  */
    private var mLockedAlpha = java.lang.Float.NaN

    /** Choreographer callback that re-draws the blur and schedules another callback.  */
    private val invalidationLoop = object : Choreographer.FrameCallback {
        override fun doFrame(frameTimeNanos: Long) {
            invalidate()
            Choreographer.getInstance().postFrameCallbackDelayed(this, (1000 / mFPS).toLong())
        }
    }

    /**
     * Casts context to Activity and attempts to create a view reference using the window decor view.
     * @return View reference for whole activity.
     */
    private val activityView: View?
        get() {
            val activity: Activity
            try {
                activity = context as Activity
            } catch (e: ClassCastException) {
                return null
            }

            return activity.window.decorView.findViewById(android.R.id.content)
        }

    /**
     * Returns the position in screen. Left abstract to allow for specific implementations such as
     * caching behavior.
     */
    private val positionInScreen: Point
        get() {
            val pointF = getPositionInScreen(this)
            return Point(pointF.x.toInt(), pointF.y.toInt())
        }

    /**
     * Get downscale factor.
     * See [.mDownscaleFactor].
     */
    /**
     * Sets downscale factor to use pre-blur.
     * See [.mDownscaleFactor].
     */
    // This field is now bad (it's pre-scaled with downscaleFactor so will need to be re-made)
    var downscaleFactor: Float
        get() = this.mDownscaleFactor
        set(downscaleFactor) {
            this.mDownscaleFactor = downscaleFactor
            this.mLockedBitmap = null

            invalidate()
        }

    /**
     * Get blur radius to use on downscaled bitmap.
     * See [.mBlurRadius].
     */
    /**
     * Sets blur radius to use on downscaled bitmap.
     * See [.mBlurRadius].
     */
    // This field is now bad (it's pre-blurred with blurRadius so will need to be re-made)
    var blurRadius: Int
        get() = this.mBlurRadius
        set(blurRadius) {
            this.mBlurRadius = blurRadius
            this.mLockedBitmap = null

            invalidate()
        }

    /**
     * Get FPS value.
     * See [.mFPS].
     */
    /**
     * Sets FPS to invalidate blur.
     * See [.mFPS].
     */
    var fps: Int
        get() = this.mFPS
        set(fps) {
            if (mRunning) {
                pauseBlur()
            }

            this.mFPS = fps

            if (mAttachedToWindow) {
                startBlur()
            }
        }

    /**
     * Get corner radius value.
     * See [.mFPS].
     */
    var cornerRadius: Float
        get() = mCornerRadius
        set(cornerRadius) {
            this.mCornerRadius = cornerRadius
            mImageView.setCornerRadius(cornerRadius)
            invalidate()
        }

    constructor(context: Context) : super(context, null) {}

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {

        if (!isInEditMode) {
            BlurKit.init(context)
        }

        val a = context.theme.obtainStyledAttributes(
                attrs,
                io.alterac.blurkit.R.styleable.BlurLayout,
                0, 0)

        try {
            mDownscaleFactor = a.getFloat(io.alterac.blurkit.R.styleable.BlurLayout_blk_downscaleFactor, DEFAULT_DOWNSCALE_FACTOR)
            mBlurRadius = a.getInteger(io.alterac.blurkit.R.styleable.BlurLayout_blk_blurRadius, DEFAULT_BLUR_RADIUS)
            mFPS = a.getInteger(io.alterac.blurkit.R.styleable.BlurLayout_blk_fps, DEFAULT_FPS)
            mCornerRadius = a.getDimension(io.alterac.blurkit.R.styleable.BlurLayout_blk_cornerRadius, DEFAULT_CORNER_RADIUS)
        } finally {
            a.recycle()
        }

        mImageView = RoundedImageView(getContext())
        mImageView.scaleType = ImageView.ScaleType.FIT_XY
        addView(mImageView)

        cornerRadius = mCornerRadius
    }

    /** Start BlurLayout continuous invalidation.  */
    fun startBlur() {
        if (mRunning) {
            return
        }

        if (mFPS > 0) {
            mRunning = true
            Choreographer.getInstance().postFrameCallback(invalidationLoop)
        }
    }

    /** Pause BlurLayout continuous invalidation.  */
    fun pauseBlur() {
        if (!mRunning) {
            return
        }

        mRunning = false
        Choreographer.getInstance().removeFrameCallback(invalidationLoop)
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        mAttachedToWindow = true
        startBlur()
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        mAttachedToWindow = false
        pauseBlur()
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        invalidate()
    }

    override fun invalidate() {
        super.invalidate()
        val bitmap = blur()
        if (bitmap != null) {
            mImageView.setImageBitmap(bitmap)
        }
    }

    /**
     * Recreates blur for content and sets it as the background.
     */
    private fun blur(): Bitmap? {
        if (context == null || isInEditMode) {
            return null
        }

        // Check the reference to the parent view.
        // If not available, attempt to make it.
        if (mActivityView == null || mActivityView!!.get() == null) {
            mActivityView = WeakReference<View>(activityView)
            if (mActivityView!!.get() == null) {
                return null
            }
        }

        val pointRelativeToActivityView: Point
        if (positionLocked) {
            // Generate a locked point if null.
            if (mLockedPoint == null) {
                mLockedPoint = positionInScreen
            }

            // Use locked point.
            pointRelativeToActivityView = mLockedPoint!!
        } else {
            // Calculate the relative point to the parent view.
            pointRelativeToActivityView = positionInScreen
        }

        // Set alpha to 0 before creating the parent view bitmap.
        // The blur view shouldn't be visible in the created bitmap.
        if (java.lang.Float.isNaN(mLockedAlpha)) {
            mLockedAlpha = alpha
        }
        super.setAlpha(0f)

        // Screen sizes for bound checks
        val screenWidth = mActivityView?.get()?.width!!
        val screenHeight = mActivityView?.get()?.height!!

        // The final dimensions of the blurred bitmap.
        val width = (width * mDownscaleFactor).toInt()
        val height = (height * mDownscaleFactor).toInt()

        // The X/Y position of where to crop the bitmap.
        val x = (pointRelativeToActivityView.x * mDownscaleFactor).toInt()
        val y = (pointRelativeToActivityView.y * mDownscaleFactor).toInt()

        // Padding to add to crop pre-blur.
        // Blurring straight to edges has side-effects so padding is added.
        val xPadding = getWidth() / 8
        val yPadding = getHeight() / 8

        // Calculate padding independently for each side, checking edges.
        var leftOffset = -xPadding
        leftOffset = if (x + leftOffset >= 0) leftOffset else 0

        var rightOffset = xPadding
        rightOffset = if (x + getWidth() + rightOffset <= screenWidth) rightOffset else screenWidth - getWidth() - x

        var topOffset = -yPadding
        topOffset = if (y + topOffset >= 0) topOffset else 0

        var bottomOffset = yPadding
        bottomOffset = if (y + height + bottomOffset <= screenHeight) bottomOffset else 0

        // Parent view bitmap, downscaled with mDownscaleFactor
        var bitmap: Bitmap
        if (viewLocked) {
            // It's possible for mLockedBitmap to be null here even with view locked.
            // lockView() should always properly set mLockedBitmap if this code is reached
            // (it passed previous checks), so recall lockView and assume it's good.
            if (mLockedBitmap == null) {
                lockView()
            }

            if (width == 0 || height == 0) {
                return null
            }

            bitmap = Bitmap.createBitmap(mLockedBitmap!!, x, y, width, height)
        } else {
            try {
                // Create parent view bitmap, cropped to the BlurLayout area with above padding.
                bitmap = getDownscaledBitmapForView(
                        mActivityView?.get()!!,
                        Rect(
                                pointRelativeToActivityView.x + leftOffset,
                                pointRelativeToActivityView.y + topOffset,
                                pointRelativeToActivityView.x + getWidth() + Math.abs(leftOffset) + rightOffset,
                                pointRelativeToActivityView.y + getHeight() + Math.abs(topOffset) + bottomOffset
                        ),
                        mDownscaleFactor
                )
            } catch (e: BlurKitException) {
                return null
            } catch (e: NullPointerException) {
                return null
            }

        }

        if (!viewLocked) {
            // Blur the bitmap.
            bitmap = BlurKit.getInstance().blur(bitmap, mBlurRadius)

            //Crop the bitmap again to remove the padding.
            bitmap = Bitmap.createBitmap(
                    bitmap,
                    (Math.abs(leftOffset) * mDownscaleFactor).toInt(),
                    (Math.abs(topOffset) * mDownscaleFactor).toInt(),
                    width,
                    height
            )

        }

        // Make self visible again.
        super.setAlpha(mLockedAlpha)

        // Set background as blurred bitmap.
        return bitmap
    }

    /**
     * Finds the Point of the parent view, and offsets result by self getX() and getY().
     * @return Point determining position of the passed in view inside all of its ViewParents.
     */
    private fun getPositionInScreen(view: View): PointF {
        if (parent == null) {
            return PointF()
        }

        val parent: ViewGroup?
        try {
            parent = view.parent as ViewGroup
        } catch (e: Exception) {
            return PointF()
        }

        if (parent == null) {
            return PointF()
        }

        val point = getPositionInScreen(parent)
        point.offset(view.x, view.y)
        return point
    }

    /**
     * Users a View reference to create a bitmap, and downscales it using the passed in factor.
     * Uses a Rect to crop the view into the bitmap.
     * @return Bitmap made from view, downscaled by downscaleFactor.
     * @throws NullPointerException
     */
    @Throws(BlurKitException::class, NullPointerException::class)
    private fun getDownscaledBitmapForView(view: View, crop: Rect, downscaleFactor: Float): Bitmap {
        val screenView = view.rootView

        val width = (crop.width() * downscaleFactor).toInt()
        val height = (crop.height() * downscaleFactor).toInt()

        if (screenView.width <= 0 || screenView.height <= 0 || width <= 0 || height <= 0) {
            throw BlurKitException("No screen available (width or height = 0)")
        }

        val dx = -crop.left * downscaleFactor
        val dy = -crop.top * downscaleFactor

        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val matrix = Matrix()
        matrix.preScale(downscaleFactor, downscaleFactor)
        matrix.postTranslate(dx, dy)
        canvas.matrix = matrix
        screenView.draw(canvas)

        return bitmap
    }

    /**
     * Save the view bitmap to be re-used each frame instead of regenerating.
     * See [.mViewLocked].
     */
    fun lockView() {
        viewLocked = true

        if (mActivityView != null && mActivityView!!.get() != null) {
            val view = mActivityView?.get()?.rootView!!
            try {
                if (java.lang.Float.isNaN(mLockedAlpha)) {
                    mLockedAlpha = alpha
                }
                super.setAlpha(0f)
                mLockedBitmap = getDownscaledBitmapForView(view, Rect(0, 0, view.width, view.height), mDownscaleFactor)
                super.setAlpha(mLockedAlpha)
                mLockedBitmap = BlurKit.getInstance().blur(mLockedBitmap, mBlurRadius)
            } catch (e: Exception) {
                // ignore
            }

        }
    }

    /**
     * Stop using saved view bitmap. View bitmap will now be re-made each frame.
     * See [.mViewLocked].
     */
    fun unlockView() {
        viewLocked = false
        mLockedBitmap = null
    }

    /**
     * Save the view position to be re-used each frame instead of regenerating.
     * See [.mPositionLocked].
     */
    fun lockPosition() {
        positionLocked = true
        mLockedPoint = positionInScreen
    }

    /**
     * Stop using saved point. Point will now be re-made each frame.
     * See [.mPositionLocked].
     */
    fun unlockPosition() {
        positionLocked = false
        mLockedPoint = null
    }

    override fun setAlpha(alpha: Float) {
        mLockedAlpha = alpha
        if (!viewLocked) {
            super.setAlpha(alpha)
        }
    }

}
