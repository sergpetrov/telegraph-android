package com.telex.base.presentation.settings

import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.Intent.ACTION_SENDTO
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import com.telex.base.BuildConfig
import com.telex.base.R
import com.telex.base.analytics.AnalyticsHelper
import com.telex.base.presentation.base.BaseActivity
import kotlinx.android.synthetic.main.activity_about.*

/**
 * @author Sergey Petrov
 */
class AboutActivity : BaseActivity() {

    override val layoutRes: Int = R.layout.activity_about

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setupStatusBar()
        closeImageView.setOnClickListener { finish() }
        versionTextView.text = BuildConfig.VERSION_NAME

        appVersionLayout.setOnClickListener {
            try {
                val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.app_link)))
                startActivity(browserIntent)
            } catch (e: ActivityNotFoundException) {
                Toast.makeText(this, getString(R.string.error_app_not_found), Toast.LENGTH_SHORT).show()
            }
        }

        bugLayout.setOnClickListener {
            try {
                val emailIntent = Intent(ACTION_SENDTO, Uri.parse("mailto:${getString(R.string.developer_email)}"))
                startActivity(Intent.createChooser(emailIntent, getString(R.string.contact_us)))
            } catch (e: ActivityNotFoundException) {
                Toast.makeText(this, getString(R.string.error_app_not_found), Toast.LENGTH_SHORT).show()
            }
        }

        channelLayout.setOnClickListener {
            try {
                val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.channel_link)))
                startActivity(browserIntent)
            } catch (e: ActivityNotFoundException) {
                Toast.makeText(this, getString(R.string.error_app_not_found), Toast.LENGTH_SHORT).show()
            }
        }

        developerLayout.setOnClickListener {
            AnalyticsHelper.logOpenAboutDeveloper()
            try {
                val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.developer_link)))
                startActivity(browserIntent)
            } catch (e: ActivityNotFoundException) {
                Toast.makeText(this, getString(R.string.error_app_not_found), Toast.LENGTH_SHORT).show()
            }
        }

        privacyPolicyLayout.setOnClickListener {
            router.showPrivacyPolicyActivity(this)
        }
    }
}
