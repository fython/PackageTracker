package info.papdt.express.helper.model

import android.content.Context
import androidx.annotation.ColorInt
import info.papdt.express.helper.R

data class MaterialPalette(
        val colorName: String,
        private val colorMap: Map<String, Int>
) {

    companion object {

        fun make(colorName: String, vararg colors: Pair<String, Int>): MaterialPalette {
            return MaterialPalette(colorName, mapOf(*colors))
        }

        fun makeFromResources(
                context: Context,
                colorName: String,
                vararg colorsRes: Pair<String, Int>
        ): MaterialPalette {
            return MaterialPalette(colorName, mapOf(*colorsRes.map {
                it.first to context.resources.getColor(it.second)
            }.toTypedArray()))
        }

    }

    @ColorInt
    operator fun get(key: String): Int {
        return colorMap.getValue(key)
    }

    @ColorInt
    fun getPackageIconBackground(context: Context): Int {
        return get(context.getString(R.string.packageIconBackgroundColorKey))
    }

    @ColorInt
    fun getPackageIconForeground(context: Context): Int {
        return get(context.getString(R.string.packageIconForegroundColorKey))
    }

    @ColorInt
    fun getStatusIconTint(context: Context): Int {
        return get(context.getString(R.string.packageStatusIconColorKey))
    }

}