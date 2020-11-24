package com.telex.base.presentation.page.dialogs

import android.app.Dialog
import android.os.Bundle
import android.view.WindowManager
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.telex.base.R
import com.telex.base.extention.toNetworkUrl
import com.telex.base.presentation.base.BaseBottomSheetFragment
import kotlinx.android.synthetic.main.dialog_add_link.*

/**
 * @author Sergey Petrov
 */
class AddLinkDialogFragment : BaseBottomSheetFragment() {
    private var onAddClickListener: ((String) -> Unit)? = null
    private var onRemoveClickListener: (() -> Unit)? = null
    private var initialUrl: String? = null
    private var showRemoveButton: Boolean = false

    override val layout: Int
        get() = R.layout.dialog_add_link

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        retainInstance = true
    }

    override fun setupView(dialog: Dialog) {
        dialog.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
        dialog.setOnShowListener {
            dialog.urlEditText.requestFocus()
            (dialog as BottomSheetDialog).behavior.state = BottomSheetBehavior.STATE_EXPANDED
        }

        when {
            showRemoveButton -> {
                dialog.titleTextView.setText(R.string.delete_link)
                dialog.submitButton.setText(R.string.delete)
                dialog.submitButton.setOnClickListener {
                    onRemoveClickListener?.invoke()
                    dialog.dismiss()
                }
            }
            else -> {
                dialog.titleTextView.setText(R.string.add_link)
                dialog.submitButton.setText(R.string.add)
                dialog.submitButton.setOnClickListener {
                    val url = dialog.urlTextInputLayout.editText?.text.toString()
                    if (dialog.urlTextInputLayout.isInputValid()) {
                        onAddClickListener?.invoke(url.toNetworkUrl())
                        dialog.dismiss()
                    }
                }
            }
        }

        initialUrl?.let { dialog.urlEditText.setText(initialUrl) }
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
