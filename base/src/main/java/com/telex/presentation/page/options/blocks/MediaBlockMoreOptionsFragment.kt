package com.telex.presentation.page.options.blocks

import android.app.Dialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import com.telex.R

/**
 * @author Sergey Petrov
 */
class MediaBlockMoreOptionsFragment : BlockMoreOptionsFragment() {
    private val url by lazy {
        arguments?.getString(URL) ?: throw IllegalArgumentException("url can't be null")
    }

    private val openOption = Option(R.drawable.ic_open_in_app, R.string.open,
            onClick = {
                startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
            }
    )

    override fun setupView(dialog: Dialog) {
        addOptions(openOption)
        super.setupView(dialog)
    }

    companion object {
        private const val URL = "URL"

        fun newInstance(url: String) = MediaBlockMoreOptionsFragment().apply {
            arguments = Bundle().apply {
                putString(URL, url)
            }
        }
    }
}
