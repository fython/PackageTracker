package info.papdt.express.helper.model

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Typeface
import android.text.TextPaint
import android.text.TextUtils
import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.Deferred
import kotlinx.coroutines.experimental.async

class MaterialIcon(val code: String) {

    fun toBitmap(size: Int): Bitmap {
        val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val textPaint = TextPaint().apply {
            style = Paint.Style.FILL_AND_STROKE
            textSize = size.toFloat()
            typeface = iconTypeface
            isAntiAlias = true
        }
        canvas.drawText(code, 0F, size.toFloat(), textPaint)
        return bitmap
    }

    fun toBitmapAsync(size: Int): Deferred<Bitmap> {
        return async(CommonPool) { toBitmap(size) }
    }

    companion object {

        lateinit var codePoints: List<String>
            private set

        lateinit var iconTypeface: Typeface
            private set

        @JvmStatic fun init(context: Context) {
            codePoints = context.resources.assets.open("codepoints")
                    .buffered()
                    .reader()
                    .readLines()
                    .map { it.split(' ')[0] }
            iconTypeface = Typeface.createFromAsset(
                    context.resources.assets, "material_icons.ttf")
        }

        @JvmStatic fun search(keyword: String?): List<String> {
            if (keyword == null || TextUtils.isEmpty(keyword)) {
                return codePoints
            }
            val keywords = keyword.split(' ')
            return codePoints.filter { codePoint -> keywords.all { it in codePoint } }
        }

    }

}