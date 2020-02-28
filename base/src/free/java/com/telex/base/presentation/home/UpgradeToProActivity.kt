package com.telex.base.presentation.home

import android.os.Bundle
import com.telex.base.R
import com.telex.base.presentation.base.BaseActivity
import com.telex.base.utils.ViewUtils
import kotlinx.android.synthetic.free.activity_upgrade_to_pro.*

/**
 * @author Sergey Petrov
 */
class UpgradeToProActivity : BaseActivity() {

    override val layoutRes: Int = R.layout.activity_upgrade_to_pro

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setupStatusBar()
        closeImageView.setOnClickListener { finish() }

        installProButton.setOnClickListener {
            ViewUtils.openPro(this)
            finish()
        }
    }
}
