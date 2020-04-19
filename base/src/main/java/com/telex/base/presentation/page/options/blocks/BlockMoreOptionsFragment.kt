package com.telex.base.presentation.page.options.blocks

import android.app.Dialog
import com.telex.base.R
import com.telex.base.presentation.base.BaseOptionsFragment

/**
 * @author Sergey Petrov
 */
open class BlockMoreOptionsFragment : BaseOptionsFragment() {

    val duplicateOption = Option(R.drawable.ic_duplicate, R.string.duplicate)
    val deleteOption = Option(R.drawable.ic_delete, R.string.delete, color = R.color.red)

    override fun setupView(dialog: Dialog) {
        super.setupView(dialog)

        addOptions(duplicateOption, deleteOption)
    }
}
