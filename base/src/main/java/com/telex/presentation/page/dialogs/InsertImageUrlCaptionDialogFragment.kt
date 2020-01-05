package com.telex.presentation.page.dialogs

import android.os.Bundle
import android.view.View
import android.view.WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE
import androidx.appcompat.app.AlertDialog
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.telex.R
import com.telex.extention.toNetworkUrl
import com.telex.presentation.base.BaseDialogFragment
import kotlinx.android.synthetic.main.dialog_image_url_caption.*

/**
 * @author Sergey Petrov
 */
class InsertImageUrlCaptionDialogFragment : BaseDialogFragment() {
    var onAddClickListener: ((String, String) -> Unit)? = null
    private var imageUrl: String? = null

    companion object {
        fun newInstance(imageUrl: String?, onAddClickListener: (String, String) -> Unit): InsertImageUrlCaptionDialogFragment {
            val fragment = InsertImageUrlCaptionDialogFragment()
            fragment.imageUrl = imageUrl
            fragment.onAddClickListener = onAddClickListener
            return fragment
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        retainInstance = true
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): AlertDialog {
        val builder = MaterialAlertDialogBuilder(context)
        builder.setView(R.layout.dialog_image_url_caption)
        builder.setTitle(R.string.add_link)
        builder.setCancelable(false)
        builder.setPositiveButton(R.string.add) { _, _ -> }
        if (imageUrl.isNullOrBlank()) {
            builder.setNegativeButton(R.string.cancel, { _, _ -> })
        } else {
            builder.setNegativeButton(R.string.skip) { _, _ ->
                onAddClickListener?.invoke(requireNotNull(imageUrl), "")
            }
        }
        val dialog = builder.create()
        dialog.window?.setSoftInputMode(SOFT_INPUT_STATE_VISIBLE)
        dialog.setOnShowListener {
            dialog.urlEditText.requestFocus()

            if (imageUrl.isNullOrBlank()) {
                dialog.urlTextInputLayout.visibility = View.VISIBLE
                dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                    val caption = dialog.captionEditText.text.toString().trim { it <= ' ' }
                    val url = dialog.urlTextInputLayout.editText?.text.toString()
                    if (dialog.urlTextInputLayout.isInputValid()) {
                        onAddClickListener?.invoke(url.toNetworkUrl(), caption)
                        dismiss()
                    }
                }
            } else {
                dialog.urlTextInputLayout.visibility = View.GONE
                dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                    val caption = dialog.captionEditText.text.toString().trim { it <= ' ' }
                    onAddClickListener?.invoke(requireNotNull(imageUrl), caption)
                    dismiss()
                }
            }
        }
        return dialog
    }
}
