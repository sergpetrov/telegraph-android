package com.telex.base.presentation.page.dialogs

import android.app.Dialog
import android.view.WindowManager
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.telex.base.R
import com.telex.base.presentation.base.BaseBottomSheetFragment
import com.telex.base.presentation.page.format.MediaFormat
import kotlinx.android.synthetic.main.dialog_iframe.*

/**
 * @author Sergey Petrov
 */
class InsertIframeDialogFragment : BaseBottomSheetFragment() {

    override val layout: Int
        get() = R.layout.dialog_iframe

    var onAddClickListener: ((MediaFormat) -> Unit)? = null

    override fun setupView(dialog: Dialog) {
        dialog.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
        dialog.setOnShowListener {
            dialog.urlEditText.requestFocus()
            (dialog as BottomSheetDialog).behavior.state = BottomSheetBehavior.STATE_EXPANDED
        }
        dialog.submitButton.setOnClickListener {
            var url = dialog.urlTextInputLayout.editText?.text.toString()
            var isValid = false
            when {
                url.matches(Regex("^(https?://)(www.)?(youtube.com/watch.*v=([a-zA-Z0-9_-]+))\$")) -> {
                    url = "/youtube?url=$url"
                    isValid = true
                }
                url.matches(Regex("^(https?://)(www.)?(youtu.?be)/([a-zA-Z0-9_-]+)\$")) -> {
                    url = "/youtube?url=$url"
                    isValid = true
                }
                url.matches(Regex("^(https?://)(www.)?(vimeo.com)/(\\d+)\$")) -> {
                    url = "/vimeo?url=$url"
                    isValid = true
                }
                url.matches(Regex("^(https?://)(www.|mobile.)?twitter.com/(.+)/status/(\\d+)\$")) -> {
                    url = "/twitter?url=$url"
                    isValid = true
                }
                url.matches(Regex("^(https?://)(t.me|telegram.me|telegram.dog)/([a-zA-Z0-9_]+)/(\\d+)\$")) -> {
                    url = "/telegram?url=$url"
                    isValid = true
                }
            }

            if (dialog.urlTextInputLayout.isInputValid { isValid }) {
                val src = "/embed$url"
                val format =
                        MediaFormat(
                                childHtml = "<iframe src=\"$src\" width=\"640\" height=\"360\" allowfullscreen=\"true\" allowtransparency=\"true\" frameborder=\"0\" scrolling=\"no\" />",
                                src = src,
                                caption = ""
                        )
                onAddClickListener?.invoke(format)
                dismiss()
            } else {
                dialog.urlTextInputLayout.isErrorEnabled = true
            }
        }
    }

    companion object {
        fun newInstance(onAddClickListener: (MediaFormat) -> Unit): InsertIframeDialogFragment {
            val fragment = InsertIframeDialogFragment()
            fragment.onAddClickListener = onAddClickListener
            return fragment
        }
    }
}
