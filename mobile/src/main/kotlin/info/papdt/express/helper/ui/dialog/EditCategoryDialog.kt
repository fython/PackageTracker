package info.papdt.express.helper.ui.dialog

import android.annotation.SuppressLint
import android.app.Activity.RESULT_OK
import android.app.AlertDialog
import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import info.papdt.express.helper.*
import info.papdt.express.helper.R
import info.papdt.express.helper.dao.SRDatabase
import info.papdt.express.helper.event.EventIntents
import info.papdt.express.helper.model.Category
import info.papdt.express.helper.model.MaterialIcon
import info.papdt.express.helper.ui.ChooseIconActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import moe.feng.kotlinyan.common.*

class EditCategoryDialog : DialogFragment() {

    companion object {

        const val REASON_EDIT = 0
        const val REASON_CREATE = 1

        const val EXTRA_REASON = "reason"

        const val REQUEST_CODE_CHOOSE_ICON = 20001

        fun newCreateDialog(): EditCategoryDialog {
            return EditCategoryDialog().apply {
                arguments = Bundle().apply {
                    putInt(EXTRA_REASON, REASON_CREATE)
                }
            }
        }

        fun newEditDialog(oldData: Category): EditCategoryDialog {
            return EditCategoryDialog().apply {
                arguments = Bundle().apply {
                    putInt(EXTRA_REASON, REASON_EDIT)
                    putParcelable(EXTRA_OLD_DATA, oldData)
                }
            }
        }

    }

    private var reason: Int = -1
    private lateinit var oldData: Category
    private lateinit var data: Category

    private lateinit var titleEdit: EditText
    private lateinit var iconView: TextView
    private var positiveButton: Button? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        arguments!!.let {
            reason = it.getInt(EXTRA_REASON, -1)
            require(reason == REASON_CREATE || reason == REASON_EDIT)
            if (reason == REASON_EDIT) {
                oldData = it.getParcelable(EXTRA_OLD_DATA)!!
            }
        }

        if (savedInstanceState == null) {
            when (reason) {
                REASON_EDIT -> {
                    data = Category(oldData)
                }
                REASON_CREATE -> {
                    data = Category(getString(R.string.category_default_title))
                }
            }
        } else {
            data = savedInstanceState.getParcelable(EXTRA_DATA)!!
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putParcelable(EXTRA_DATA, data)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return AlertDialog.Builder(context).apply {
            titleRes = when (reason) {
                REASON_CREATE -> R.string.edit_category_dialog_title_for_create
                REASON_EDIT ->  R.string.edit_category_dialog_title_for_edit
                else -> throw IllegalArgumentException()
            }
            view = createContentView()
            positiveButton(R.string.save) { _, _ ->
                when (reason) {
                    REASON_CREATE -> {
                        sendLocalBroadcast(EventIntents.saveNewCategory(data))
                    }
                    REASON_EDIT -> {
                        sendLocalBroadcast(EventIntents.saveEditCategory(oldData, data))
                    }
                }
            }
            cancelButton()
            if (reason == REASON_EDIT) {
                neutralButton(R.string.delete) { _, _ ->
                    sendLocalBroadcast(EventIntents.requestDeleteCategory(data))
                }
            }
        }.create().apply {
            setOnShowListener {
                this@EditCategoryDialog.positiveButton = (it as AlertDialog).positiveButton
                checkEnabledState()
            }
        }
    }

    @SuppressLint("InflateParams")
    private fun createContentView(): View {
        val view = LayoutInflater.from(context!!).inflate(R.layout.dialog_edit_category, null)
        titleEdit = view.findViewById(R.id.category_title_edit)
        iconView = view.findViewById(R.id.category_icon_view)

        titleEdit.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {

            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                data.title = s?.toString()?.trim() ?: ""
                checkEnabledState()
            }
        })

        iconView.typeface = MaterialIcon.iconTypeface
        updateViewValues()
        titleEdit.setSelection(titleEdit.text.length)

        val chooseBtn = view.findViewById<Button>(R.id.choose_btn)
        chooseBtn.setOnClickListener {
            if (!isDetached) {
                val intent = Intent(it.context, ChooseIconActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_MULTIPLE_TASK)
                startActivityForResult(intent, REQUEST_CODE_CHOOSE_ICON)
            }
        }

        return view
    }

    private fun updateViewValues() {
        titleEdit.setText(data.title)
        iconView.text = data.iconCode
    }

    private fun checkEnabledState() {
        CoroutineScope(Dispatchers.Main).launch {
            var shouldEnabled = !data.title.isEmpty()
            if (shouldEnabled) {
                when (reason) {
                    REASON_CREATE -> {
                        if (SRDatabase.categoryDao.get(data.title) != null) {
                            shouldEnabled = false
                        }
                    }
                    REASON_EDIT -> {
                        if (oldData.title != data.title
                                && SRDatabase.categoryDao.get(data.title) != null) {
                            shouldEnabled = false
                        }
                    }
                }
            }
            positiveButton?.isEnabled = shouldEnabled
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, intent: Intent?) {
        when (requestCode) {
            REQUEST_CODE_CHOOSE_ICON -> {
                if (resultCode == RESULT_OK && intent != null) {
                    data.iconCode = intent[ChooseIconActivity.EXTRA_RESULT_ICON_CODE]!!.asString()
                    updateViewValues()
                }
            }
        }
    }

    private fun sendLocalBroadcast(intent: Intent) {
        context?.let { LocalBroadcastManager.getInstance(it).sendBroadcast(intent) }
    }

}