package com.telex.base.presentation.page.dialogs

import android.os.Bundle
import android.view.WindowManager
import androidx.appcompat.app.AlertDialog
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.telex.base.R
import com.telex.base.presentation.base.BaseDialogFragment
import com.telex.base.presentation.page.format.MediaFormat
import kotlinx.android.synthetic.main.dialog_iframe.*

/**
 * @author Sergey Petrov
 */
class InsertIframeDialogFragment : BaseDialogFragment() {
    var onAddClickListener: ((MediaFormat) -> Unit)? = null

    companion object {
        fun newInstance(onAddClickListener: (MediaFormat) -> Unit): InsertIframeDialogFragment {
            val fragment = InsertIframeDialogFragment()
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
        builder.setView(R.layout.dialog_iframe)
        builder.setTitle(R.string.add_iframe)
        builder.setNegativeButton(R.string.cancel, null)
        builder.setPositiveButton(R.string.add, null)
        val dialog = builder.create()
        dialog.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE)
        dialog.setOnShowListener {
            dialog.urlEditText.requestFocus()

            dialog.setButton(AlertDialog.BUTTON_POSITIVE, getString(R.string.add)) { _, _ -> }
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
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

                if (dialog.urlTextInputLayout.isInputValid({ isValid })) {
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
        return dialog
    }
}
