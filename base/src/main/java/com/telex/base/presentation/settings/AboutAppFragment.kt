package com.telex.base.presentation.settings

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import com.telex.base.BuildConfig
import com.telex.base.R
import com.telex.base.analytics.AnalyticsHelper
import com.telex.base.extention.applySystemWindowInsetsPadding
import com.telex.base.presentation.base.BaseFragment
import kotlinx.android.synthetic.main.fragment_about.*

/**
 * @author Sergey Petrov
 */
class AboutAppFragment : BaseFragment() {

    override val layoutRes: Int = R.layout.fragment_about

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        rootLayout.applySystemWindowInsetsPadding(applyTop = true, applyBottom = true)

        closeImageView.setOnClickListener { findNavController().popBackStack() }
        versionTextView.text = BuildConfig.VERSION_NAME

        appVersionLayout.setOnClickListener {
            try {
                val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.app_link)))
                startActivity(browserIntent)
            } catch (e: ActivityNotFoundException) {
                Toast.makeText(context, getString(R.string.error_app_not_found), Toast.LENGTH_SHORT).show()
            }
        }

        bugLayout.setOnClickListener {
            try {
                val emailIntent = Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:${getString(R.string.developer_email)}"))
                startActivity(Intent.createChooser(emailIntent, getString(R.string.contact_us)))
            } catch (e: ActivityNotFoundException) {
                Toast.makeText(context, getString(R.string.error_app_not_found), Toast.LENGTH_SHORT).show()
            }
        }

        channelLayout.setOnClickListener {
            try {
                val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.channel_link)))
                startActivity(browserIntent)
            } catch (e: ActivityNotFoundException) {
                Toast.makeText(context, getString(R.string.error_app_not_found), Toast.LENGTH_SHORT).show()
            }
        }

        developerLayout.setOnClickListener {
            AnalyticsHelper.logOpenAboutDeveloper()
            try {
                val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.developer_link)))
                startActivity(browserIntent)
            } catch (e: ActivityNotFoundException) {
                Toast.makeText(context, getString(R.string.error_app_not_found), Toast.LENGTH_SHORT).show()
            }
        }

        privacyPolicyLayout.setOnClickListener {
            findNavController().navigate(R.id.privacyPolicyFragment)
        }
    }
}
