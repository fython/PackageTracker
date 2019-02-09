package info.papdt.express.helper.support

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.os.Build
import android.util.TypedValue

object ScreenUtils {

    fun isChrome(): Boolean {
        return Build.BRAND == "chromium" || Build.BRAND == "chrome"
    }

    fun getStatusBarHeight(context: Context): Int {
        var result = 0
        val resourceId = context.resources.getIdentifier("status_bar_height", "dimen", "android")
        if (resourceId > 0) {
            result = context.resources.getDimensionPixelSize(resourceId)
        }
        return result
    }

    private fun getMiddleValue(prev: Int, next: Int, factor: Float): Int {
        return Math.round(prev + (next - prev) * factor)
    }

    fun getMiddleColor(prevColor: Int, curColor: Int, factor: Float): Int {
        if (prevColor == curColor) {
            return curColor
        }

        if (factor == 0f) {
            return prevColor
        } else if (factor == 1f) {
            return curColor
        }

        val a = getMiddleValue(Color.alpha(prevColor), Color.alpha(curColor), factor)
        val r = getMiddleValue(Color.red(prevColor), Color.red(curColor), factor)
        val g = getMiddleValue(Color.green(prevColor), Color.green(curColor), factor)
        val b = getMiddleValue(Color.blue(prevColor), Color.blue(curColor), factor)

        return Color.argb(a, r, g, b)
    }

    fun getColor(baseColor: Int, alphaPercent: Float): Int {
        val alpha = Math.round(Color.alpha(baseColor) * alphaPercent)

        return baseColor and 0x00FFFFFF or (alpha shl 24)
    }

    fun dpToPx(context: Context, dp: Float): Float {
        return TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                dp,
                context.resources.displayMetrics
        ) + 0.5f
    }

    fun drawableToBitmap(drawable: Drawable): Bitmap {
        if (drawable is BitmapDrawable) {
            if (drawable.bitmap != null) {
                return drawable.bitmap
            }
        }

        val bitmap = if (drawable.intrinsicWidth <= 0 || drawable.intrinsicHeight <= 0) {
            Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888) // Single color bitmap will be created of 1x1 pixel
        } else {
            Bitmap.createBitmap(drawable.intrinsicWidth, drawable.intrinsicHeight, Bitmap.Config.ARGB_8888)
        }

        val canvas = Canvas(bitmap!!)
        drawable.setBounds(0, 0, canvas.width, canvas.height)
        drawable.draw(canvas)
        return bitmap
    }

}
