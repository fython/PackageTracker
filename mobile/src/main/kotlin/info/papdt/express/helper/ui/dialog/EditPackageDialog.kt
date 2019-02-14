package info.papdt.express.helper.ui.dialog

import android.app.Dialog
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import com.google.android.material.snackbar.Snackbar
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import info.papdt.express.helper.R
import info.papdt.express.helper.RESULT_RENAMED
import info.papdt.express.helper.dao.PackageDatabase
import info.papdt.express.helper.dao.SRDatabase
import info.papdt.express.helper.model.Kuaidi100Package
import info.papdt.express.helper.model.MaterialIcon
import info.papdt.express.helper.ui.ChooseIconActivity
import info.papdt.express.helper.ui.DetailsActivity
import info.papdt.express.helper.ui.common.AbsDialogFragment
import moe.feng.kotlinyan.common.*
import org.jetbrains.anko.bundleOf
import kotlin.concurrent.thread

class EditPackageDialog : AbsDialogFragment() {

    private lateinit var nameEdit: EditText
    private lateinit var categoryIcon: TextView
    private lateinit var categoryTitle: TextView

    private lateinit var data: Kuaidi100Package
    private var currentCategory: String? = null

    private val chooseCategoryCallback = ChooseCategoryDialog.onChooseCategory {
        currentCategory = if (it.title.isEmpty()) null else it.title
        updateCategoryViews()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        data = arguments!!.getParcelable(ARG_DATA)!!
        currentCategory = data.categoryTitle
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return buildV7AlertDialog {
            titleRes = R.string.dialog_edit_name_title
            view = createContentView()
            okButton { _, _ -> ui {
                if (!TextUtils.isEmpty(nameEdit.text.toString())) {
                    data.name = nameEdit.text.toString().trim { it <= ' ' }
                    data.categoryTitle = currentCategory
                    (requireActivity() as DetailsActivity).setUpData()

                    val db = PackageDatabase.getInstance(requireContext())
                    db[db.indexOf(data.number!!)] = data
                    db.save()

                    val intent = Intent()
                    intent["id"] = data.number
                    requireActivity().setResult(RESULT_RENAMED, intent)
                } else {
                    Snackbar.make(requireActivity().findViewById(R.id.coordinator_layout)!!,
                            R.string.toast_edit_name_is_empty, Snackbar.LENGTH_SHORT)
                            .show()
                }
            } }
            cancelButton()
        }
    }

    private fun createContentView(): View {
        val view = LayoutInflater.from(context!!).inflate(R.layout.dialog_edit_package, null)

        nameEdit = view.findViewById(R.id.name_edit)
        view.findViewById<Button>(R.id.icon_choose_button).setOnClickListener {
            ChooseCategoryDialog().show(fragmentManager, "choose_category")
        }
        nameEdit.setText(data.name!!)
        nameEdit.setSelection(data.name!!.length)

        categoryIcon = view.findViewById(R.id.category_icon_view)
        categoryTitle = view.findViewById(R.id.category_title_view)

        categoryIcon.typeface = MaterialIcon.iconTypeface

        updateCategoryViews()

        return view
    }

    private fun updateCategoryViews() = ui {
        currentCategory?.let {
            categoryIcon.text = SRDatabase.categoryDao.get(it)?.iconCode ?: ""
            categoryTitle.text = it
        } ?: run {
            categoryIcon.text = ""
            categoryTitle.setText(R.string.choose_category_dialog_unclassified)
        }
    }

    override fun onStart() {
        super.onStart()
        LocalBroadcastManager.getInstance(context!!).registerReceiver(
                chooseCategoryCallback, IntentFilter(ChooseCategoryDialog.ACTION_CHOOSE_CATEGORY))
    }

    override fun onStop() {
        super.onStop()
        LocalBroadcastManager.getInstance(context!!).unregisterReceiver(chooseCategoryCallback)
    }

    companion object {

        const val ARG_DATA = "arg_data"

        const val REQUEST_CODE_CHOOSE_ICON = 10

        @JvmStatic fun newInstance(data: Kuaidi100Package): EditPackageDialog {
            return EditPackageDialog().apply {
                arguments = bundleOf(ARG_DATA to Kuaidi100Package(data))
            }
        }

    }

}