package com.telex.presentation.home

import android.os.Bundle
import com.telex.R
import com.telex.presentation.base.BaseActivity
import com.telex.utils.ViewUtils
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
