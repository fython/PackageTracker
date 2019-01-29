package info.papdt.express.helper.support

import androidx.annotation.AttrRes
import android.content.res.ColorStateList
import android.content.res.Resources
import androidx.annotation.ColorInt
import android.util.TypedValue

object ResourcesUtils {

    fun getTypedValue(theme: Resources.Theme, attrId: Int): TypedValue {
        val typedValue = TypedValue()
        theme.resolveAttribute(attrId, typedValue, true)
        return typedValue
    }

    @ColorInt
    fun getColorIntFromAttr(theme: Resources.Theme, @AttrRes attrId: Int): Int {
        return theme.resources.getColor(getTypedValue(theme, attrId).resourceId)
    }

    fun getColorStateListFromAttr(theme: Resources.Theme, @AttrRes attrId: Int): ColorStateList {
        return theme.resources.getColorStateList(getTypedValue(theme, attrId).resourceId)
    }

}