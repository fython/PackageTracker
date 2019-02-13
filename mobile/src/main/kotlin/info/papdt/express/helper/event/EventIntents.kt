package info.papdt.express.helper.event

import android.content.Intent
import android.os.Parcelable
import androidx.recyclerview.widget.RecyclerView
import info.papdt.express.helper.ACTION_PREFIX
import info.papdt.express.helper.ACTION_REQUEST_DELETE_PACK
import info.papdt.express.helper.EXTRA_DATA
import info.papdt.express.helper.EXTRA_OLD_DATA
import info.papdt.express.helper.model.Category
import info.papdt.express.helper.model.Kuaidi100Package
import me.drakeet.multitype.ItemViewBinder
import kotlin.reflect.KClass

object EventIntents {

    const val ACTION_REQUEST_DELETE_CATEGORY = "$ACTION_PREFIX.REQUEST_DELETE_CATEGORY"
    const val ACTION_SAVE_NEW_CATEGORY = "$ACTION_PREFIX.SAVE_NEW_CATEGORY"
    const val ACTION_SAVE_EDIT_CATEGORY = "$ACTION_PREFIX.SAVE_EDIT_CATEGORY"

    fun requestDeletePackage(data: Kuaidi100Package): Intent {
        val intent = Intent(ACTION_REQUEST_DELETE_PACK)
        intent.putExtra(EXTRA_DATA, data)
        return intent
    }

    fun requestDeleteCategory(data: Category): Intent {
        val intent = Intent(ACTION_REQUEST_DELETE_CATEGORY)
        intent.putExtra(EXTRA_DATA, data)
        return intent
    }

    fun saveNewCategory(data: Category): Intent {
        val intent = Intent(ACTION_SAVE_NEW_CATEGORY)
        intent.putExtra(EXTRA_DATA, data)
        return intent
    }

    fun saveEditCategory(oldData: Category, data: Category): Intent {
        val intent = Intent(ACTION_SAVE_EDIT_CATEGORY)
        intent.putExtra(EXTRA_OLD_DATA, oldData)
        intent.putExtra(EXTRA_DATA, data)
        return intent
    }

    fun <T, VH : RecyclerView.ViewHolder, B: ItemViewBinder<T, VH>>
            getItemOnClickActionName(clazz: KClass<B>): String {
        return clazz::java.name + ".ACTION_ON_CLICK"
    }

    fun <T : Parcelable, VH : RecyclerView.ViewHolder, B: ItemViewBinder<T, VH>>
            notifyItemOnClick(clazz: KClass<B>, data: T?): Intent {
        val intent = Intent(getItemOnClickActionName(clazz))
        intent.putExtra(EXTRA_DATA, data)
        return intent
    }

}