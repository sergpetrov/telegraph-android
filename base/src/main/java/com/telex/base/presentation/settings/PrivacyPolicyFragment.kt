package com.telex.base.presentation.settings

import android.os.Bundle
import android.view.View
import androidx.navigation.fragment.findNavController
import com.telex.base.R
import com.telex.base.extention.applySystemWindowInsetsPadding
import com.telex.base.presentation.base.BaseFragment
import kotlinx.android.synthetic.main.fragment_privacy_policy.*

/**
 * @author Sergey Petrov
 */
class PrivacyPolicyFragment : BaseFragment() {

    override val layoutRes: Int = R.layout.fragment_privacy_policy

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        rootLayout.applySystemWindowInsetsPadding(applyTop = true, applyBottom = true)

        closeImageView.setOnClickListener { findNavController().popBackStack() }
        webView.loadUrl("file:///android_asset/privacy_policy.html")
    }
}
