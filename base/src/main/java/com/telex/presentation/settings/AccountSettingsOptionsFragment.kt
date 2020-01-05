package com.telex.presentation.settings

import android.app.Dialog
import com.telex.R
import com.telex.presentation.base.BaseOptionsFragment

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
