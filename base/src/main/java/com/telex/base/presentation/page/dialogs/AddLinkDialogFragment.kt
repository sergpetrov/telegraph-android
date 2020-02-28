package com.telex.base.presentation.page.dialogs

import android.os.Bundle
import android.view.LayoutInflater
import android.view.WindowManager
import androidx.appcompat.app.AlertDialog
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.telex.base.R
import com.telex.base.extention.toNetworkUrl
import com.telex.base.presentation.base.BaseDialogFragment
import kotlinx.android.synthetic.main.dialog_add_link.*
import kotlinx.android.synthetic.main.dialog_add_link.view.*

/**
 * @author Sergey Petrov
 */
class AddLinkDialogFragment : BaseDialogFragment() {
    private var onAddClickListener: ((String) -> Unit)? = null
    private var onRemoveClickListener: (() -> Unit)? = null
    private var initialUrl: String? = null
    private var showRemoveButton: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        retainInstance = true
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): AlertDialog {
        val builder = MaterialAlertDialogBuilder(context)
        val view = LayoutInflater.from(context).inflate(R.layout.dialog_add_link, null)
        builder.setView(view)
        builder.setNegativeButton(R.string.cancel, null)

        if (showRemoveButton) {
            builder.setTitle(R.string.delete_link)
            builder.setPositiveButton(R.string.delete, null)
        } else {
            builder.setTitle(R.string.add_link)
            builder.setPositiveButton(R.string.add, null)
        }

        val dialog = builder.create()
        dialog.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE)
        dialog.setOnShowListener {
            dialog.urlEditText.requestFocus()

            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                if (showRemoveButton) {
                    onRemoveClickListener?.invoke()
                    dialog.dismiss()
                } else {
                    val url = dialog.urlTextInputLayout.editText?.text.toString()
                    if (dialog.urlTextInputLayout.isInputValid()) {
                        onAddClickListener?.invoke(url.toNetworkUrl())
                        dialog.dismiss()
                    }
                }
            }
        }
        initialUrl?.let { view.urlEditText.setText(initialUrl) }
        return dialog
    }

    companion object {
        fun newInstance(initialUrl: String, onAddClickListener: (String) -> Unit, onRemoveClickListener: () -> Unit, showRemoveButton: Boolean): AddLinkDialogFragment {
            val fragment = AddLinkDialogFragment()
            fragment.initialUrl = initialUrl
            fragment.showRemoveButton = showRemoveButton
            fragment.onAddClickListener = onAddClickListener
            fragment.onRemoveClickListener = onRemoveClickListener
            return fragment
        }
    }
}
