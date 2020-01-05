package com.telex.presentation.page.options

import android.app.Dialog
import com.telex.R
import com.telex.presentation.base.BaseOptionsFragment

/**
 * @author Sergey Petrov
 */
class InsertImageOptionsFragment : BaseOptionsFragment() {

    override val title: Int
        get() = R.string.insert_image

    val fromGalleryOption = Option(R.drawable.ic_image, R.string.from_gallery)
    val byUrlOption = Option(R.drawable.ic_insert_link, R.string.by_url)

    override fun setupView(dialog: Dialog) {
        super.setupView(dialog)

        addOptions(fromGalleryOption, byUrlOption)
    }
}
