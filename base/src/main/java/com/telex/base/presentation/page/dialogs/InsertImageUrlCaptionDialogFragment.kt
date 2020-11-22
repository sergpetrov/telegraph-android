package com.telex.base.presentation.page.dialogs

import android.app.Dialog
import android.view.WindowManager
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.telex.base.R
import com.telex.base.extention.toNetworkUrl
import com.telex.base.presentation.base.BaseBottomSheetFragment
import kotlinx.android.synthetic.main.dialog_image_url_caption.*

/**
 * @author Sergey Petrov
 */
class InsertImageUrlCaptionDialogFragment : BaseBottomSheetFragment() {
    var onAddClickListener: ((String, String) -> Unit)? = null
    private var imageUrl: String? = null

    override val layout: Int
        get() = R.layout.dialog_image_url_caption

    override fun setupView(dialog: Dialog) {
        dialog.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
        dialog.setOnShowListener {
            dialog.urlEditText.requestFocus()
            (dialog as BottomSheetDialog).behavior.state = BottomSheetBehavior.STATE_EXPANDED
        }
        dialog.submitButton.setOnClickListener {
            val caption = dialog.captionEditText.text.toString().trim { it <= ' ' }
            val url = dialog.urlTextInputLayout.editText?.text.toString()
            if (dialog.urlTextInputLayout.isInputValid()) {
                onAddClickListener?.invoke(url.toNetworkUrl(), caption)
                dismiss()
            }
        }
    }

    companion object {
        fun newInstance(imageUrl: String?, onAddClickListener: (String, String) -> Unit): InsertImageUrlCaptionDialogFragment {
            val fragment = InsertImageUrlCaptionDialogFragment()
            fragment.imageUrl = imageUrl
            fragment.onAddClickListener = onAddClickListener
            return fragment
        }
    }
}
