package info.papdt.express.helper.ui.fragment.dialog

import android.app.Activity.RESULT_OK
import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v4.app.DialogFragment
import android.text.TextUtils
import android.view.LayoutInflater
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import info.papdt.express.helper.R
import info.papdt.express.helper.RESULT_RENAMED
import info.papdt.express.helper.dao.PackageDatabase
import info.papdt.express.helper.model.Kuaidi100Package
import info.papdt.express.helper.model.MaterialIcon
import info.papdt.express.helper.ui.ChooseIconActivity
import info.papdt.express.helper.ui.DetailsActivity
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.async
import moe.feng.kotlinyan.common.*
import org.jetbrains.anko.bundleOf
import kotlin.concurrent.thread

class EditPackageDialog : DialogFragment() {

    private lateinit var mNameEdit: EditText
    private lateinit var mIconView: ImageView

    private lateinit var data: Kuaidi100Package
    private var currentIconCode: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        data = arguments!!.getParcelable(ARG_DATA)
        currentIconCode = data.iconCode
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val view = LayoutInflater.from(requireActivity())
                .inflate(R.layout.dialog_content_view_edit_package, null)
        mIconView = view.findViewById(R.id.icon_view)
        mNameEdit = view.findViewById(R.id.name_edit)
        view.findViewById<Button>(R.id.icon_choose_button).setOnClickListener {
            startActivityForResult(
                    Intent(requireContext(), ChooseIconActivity::class.java),
                    REQUEST_CODE_CHOOSE_ICON
            )
        }
        view.findViewById<Button>(R.id.icon_clear_button).setOnClickListener {
            currentIconCode = null
            updateIconView()
        }
        mNameEdit.setText(data.name!!)
        mNameEdit.setSelection(data.name!!.length)
        updateIconView()
        return buildV7AlertDialog {
            titleRes = R.string.dialog_edit_name_title
            setView(view)
            okButton { _, _ ->
                if (!TextUtils.isEmpty(mNameEdit.text.toString())) {
                    data.name = mNameEdit.text.toString().trim { it <= ' ' }
                    data.iconCode = currentIconCode
                    (requireActivity() as DetailsActivity).setUpData()

                    val intent = Intent()
                    intent["id"] = data.number
                    requireActivity().setResult(RESULT_RENAMED, intent)

                    thread {
                        val db = PackageDatabase.getInstance(requireContext())
                        db[db.indexOf(data.number!!)] = data
                        db.save()
                    }
                } else {
                    Snackbar.make(requireActivity().findViewById(R.id.coordinator_layout)!!,
                            R.string.toast_edit_name_is_empty, Snackbar.LENGTH_SHORT)
                            .show()
                }
            }
            cancelButton()
        }
    }

    private fun updateIconView() {
        async(UI) {
            mIconView.setImageBitmap(currentIconCode?.let {
                MaterialIcon(it).toBitmapAsync(
                        resources.getDimensionPixelSize(R.dimen.icon_size_medium))
                        .await()
            })
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == REQUEST_CODE_CHOOSE_ICON && resultCode == RESULT_OK
                && data != null) {
            currentIconCode = data.getStringExtra(ChooseIconActivity.EXTRA_RESULT_ICON_CODE)
            updateIconView()
        }
    }

    companion object {

        const val ARG_DATA = "arg_data"

        const val REQUEST_CODE_CHOOSE_ICON = 10

        @JvmStatic fun newInstance(data: Kuaidi100Package): EditPackageDialog {
            return EditPackageDialog().apply {
                arguments = bundleOf(ARG_DATA to data)
            }
        }

    }

}