package info.papdt.express.helper.ui.dialog

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.Dialog
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.recyclerview.widget.RecyclerView
import info.papdt.express.helper.ACTION_PREFIX
import info.papdt.express.helper.EXTRA_DATA
import info.papdt.express.helper.R
import info.papdt.express.helper.dao.SRDatabase
import info.papdt.express.helper.event.EventCallbacks
import info.papdt.express.helper.event.EventIntents
import info.papdt.express.helper.model.Category
import info.papdt.express.helper.ui.adapter.CategoriesListAdapter
import info.papdt.express.helper.ui.common.AbsDialogFragment
import info.papdt.express.helper.ui.items.CategoryItemViewBinder
import moe.feng.kotlinyan.common.cancelButton
import moe.feng.kotlinyan.common.titleRes
import moe.feng.kotlinyan.common.view

class ChooseCategoryDialog : AbsDialogFragment() {

    companion object {

        const val ACTION_CHOOSE_CATEGORY = "$ACTION_PREFIX.CHOOSE_CATEGORY_RESULT"

        fun onChooseCategory(callback: (Category) -> Unit): BroadcastReceiver {
            return object : BroadcastReceiver() {
                override fun onReceive(context: Context, intent: Intent?) {
                    intent?.getParcelableExtra<Category>(EXTRA_DATA)?.let(callback)
                }
            }
        }

    }

    private lateinit var listView: RecyclerView
    private val listAdapter: CategoriesListAdapter =
            CategoriesListAdapter(R.layout.item_category_for_dialog)

    private val callbackReceiver = EventCallbacks.onItemClick(
            CategoryItemViewBinder::class, ::onCategoryItemClick)

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return AlertDialog.Builder(context!!).apply {
            titleRes = R.string.choose_category_dialog_title
            view = createContentView()
            cancelButton()
        }.create()
    }

    @SuppressLint("InflateParams")
    private fun createContentView(): View {
        val view = LayoutInflater.from(context!!).inflate(R.layout.dialog_choose_category, null)
        ui {
            listView = view.findViewById(android.R.id.list)
            listView.adapter = listAdapter
            listAdapter.setCategories(asyncIO {
                mutableListOf(Category("")) + SRDatabase.categoryDao.getAll()
            }.await())
        }
        return view
    }

    private fun onCategoryItemClick(category: Category?) {
        category?.let {
            LocalBroadcastManager.getInstance(context!!)
                    .sendBroadcast(Intent(ACTION_CHOOSE_CATEGORY).putExtra(EXTRA_DATA, it))
        }
        dismiss()
    }

    override fun onStart() {
        super.onStart()
        LocalBroadcastManager.getInstance(context!!).registerReceiver(callbackReceiver,
                IntentFilter(EventIntents.getItemOnClickActionName(CategoryItemViewBinder::class)))
    }

    override fun onStop() {
        super.onStop()
        LocalBroadcastManager.getInstance(context!!).unregisterReceiver(callbackReceiver)
    }

}