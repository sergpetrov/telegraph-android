package com.telex.presentation.settings

import android.os.Bundle
import com.telex.R
import com.telex.presentation.base.BaseActivity
import kotlinx.android.synthetic.main.activity_privacy_policy.*

/**
 * @author Sergey Petrov
 */
class PrivacyPolicyActivity : BaseActivity() {

    override val layoutRes: Int = R.layout.activity_privacy_policy

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setupStatusBar()
        closeImageView.setOnClickListener { finish() }
        webView.loadUrl("file:///android_asset/privacy_policy.html")
    }
}
