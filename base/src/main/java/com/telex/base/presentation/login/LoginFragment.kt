package com.telex.base.presentation.login

import android.os.Bundle
import android.text.style.ClickableSpan
import android.view.View
import androidx.navigation.fragment.findNavController
import com.telex.base.R
import com.telex.base.analytics.AnalyticsHelper
import com.telex.base.presentation.base.BaseFragment
import com.telex.base.utils.ViewUtils
import kotlinx.android.synthetic.main.fragment_login.view.*

/**
 * @author Sergey Petrov
 */
class LoginFragment: BaseFragment() {

    override val layoutRes: Int
        get() = R.layout.fragment_login

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        ViewUtils.createLink(
            view.privacyPolicyDescriptionTextView,
            getString(R.string.privacy_policy_description),
            getString(R.string.privacy_policy_description_part_to_click),
            clickableAction = object : ClickableSpan() {
                override fun onClick(widget: View) {
                    findNavController().navigate(R.id.privacyPolicyFragment)
                }
            })

        view.launchButton.setOnClickListener {
            openTelegram()
        }
    }

    private fun openTelegram() {
        AnalyticsHelper.logLaunchTelegram()
        ViewUtils.openTelegramToLogin(requireActivity())
    }
}