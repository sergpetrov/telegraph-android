package com.telex.base.presentation.settings

import android.app.Dialog
import com.telex.base.R
import com.telex.base.presentation.base.BaseOptionsFragment

/**
 * @author Sergey Petrov
 */
class AccountSettingsOptionsFragment : BaseOptionsFragment() {

    val logoutOption = Option(R.drawable.ic_exit, R.string.logout)

    override fun setupView(dialog: Dialog) {
        super.setupView(dialog)

        addOption(logoutOption)
    }
}
