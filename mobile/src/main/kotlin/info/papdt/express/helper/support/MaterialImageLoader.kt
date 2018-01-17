package info.papdt.express.helper.support

import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.drawable.Drawable
import android.view.View
import android.widget.ImageView

class MaterialImageLoader private constructor(internal val imageView: ImageView) {

    internal val drawable: Drawable = imageView.drawable
    private var duration = DEFAULT_DURATION
    private var saturation: Float = 0.toFloat()
    private lateinit var animationSaturation: ValueAnimator
    private lateinit var animationContrast: ValueAnimator
    private lateinit var animationAlpha: ObjectAnimator

    fun getDuration(): Int {
        return duration
    }

    fun setDuration(duration: Int): MaterialImageLoader {
        this.duration = duration
        return this
    }

    fun start() {
        setup(duration)

        animationSaturation.start()
        animationContrast.start()
        animationAlpha.start()
    }

    fun cancel() {
        animationSaturation.cancel()
        animationContrast.cancel()
        animationAlpha.cancel()
    }

    private fun setContrast(contrast: Float): ColorMatrix {
        val scale = contrast + 1f
        val translate = (-.5f * scale + .5f) * 255f
        val array = floatArrayOf(scale, 0f, 0f, 0f, translate, 0f, scale, 0f, 0f, translate, 0f, 0f, scale, 0f, translate, 0f, 0f, 0f, 1f, 0f)
        return ColorMatrix(array)
    }

    private fun setup(duration: Int) {
        animationSaturation = ValueAnimator.ofFloat(0.2f, 1f)
        animationSaturation.duration = duration.toLong()
        animationSaturation.addUpdateListener { animation -> saturation = animation.animatedFraction }

        animationContrast = ValueAnimator.ofFloat(0f, 1f)
        animationContrast.duration = (duration * 3f / 4f).toLong()
        animationContrast.addUpdateListener { animation ->
            val colorMatrix = setContrast(animation.animatedFraction)
            colorMatrix.setSaturation(saturation)
            val colorFilter = ColorMatrixColorFilter(colorMatrix)
            drawable.colorFilter = colorFilter
        }

        animationAlpha = ObjectAnimator.ofFloat(imageView, View.ALPHA, 0f, 1f)
        animationAlpha.duration = (duration / 2f).toLong()
    }

    companion object {

        private const val DEFAULT_DURATION = 3000

        fun animate(imageView: ImageView): MaterialImageLoader {
            return MaterialImageLoader(imageView)
        }
    }

}