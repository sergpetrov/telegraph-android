package com.telex.presentation.settings

import android.app.Dialog
import com.telex.R
import com.telex.presentation.base.BaseOptionsFragment

/**
 * @author Sergey Petrov
 */
class ProxyServerOptionsFragment : BaseOptionsFragment() {

    val deleteOption = Option(R.drawable.ic_delete, R.string.delete)

    override fun setupView(dialog: Dialog) {
        super.setupView(dialog)

        addOption(deleteOption)
    }
}
