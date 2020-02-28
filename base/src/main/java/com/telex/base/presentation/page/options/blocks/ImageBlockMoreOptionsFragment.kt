package com.telex.base.presentation.page.options.blocks

import android.app.Dialog
import android.os.Bundle
import com.telex.base.R
import com.telex.base.utils.ViewUtils.Companion.copyToClipboard

/**
 * @author Sergey Petrov
 */
class ImageBlockMoreOptionsFragment : BlockMoreOptionsFragment() {
    private val url by lazy { arguments?.getString(URL) ?: throw IllegalArgumentException("url can't be null") }

    private val copyLinkOption = Option(R.drawable.ic_copy, R.string.copy_image_link, onClick = { copyToClipboard(context, url) })

    override fun setupView(dialog: Dialog) {
        addOptions(copyLinkOption)
        super.setupView(dialog)
    }

    companion object {
        private const val URL = "URL"

        fun newInstance(url: String) = ImageBlockMoreOptionsFragment().apply {
            arguments = Bundle().apply {
                putString(URL, url)
            }
        }
    }
}
