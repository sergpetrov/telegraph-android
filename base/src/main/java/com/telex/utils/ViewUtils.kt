package com.telex.utils

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.Intent.ACTION_VIEW
import android.net.Uri
import android.text.SpannableString
import android.text.Spanned
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.widget.TextView
import android.widget.Toast
import com.telex.R

/**
 * @author Sergey Petrov
 */
class ViewUtils {
    companion object {
        fun createLink(
            targetTextView: TextView,
            completeString: String,
            partToClick: String,
            clickableAction: ClickableSpan
        ): TextView {

            val spannableString = SpannableString(completeString)

            val startPosition = completeString.indexOf(partToClick)
            val endPosition = completeString.lastIndexOf(partToClick) + partToClick.length

            spannableString.setSpan(clickableAction, startPosition, endPosition, Spanned.SPAN_INCLUSIVE_EXCLUSIVE)

            targetTextView.text = spannableString
            targetTextView.movementMethod = LinkMovementMethod.getInstance()

            return targetTextView
        }

        fun copyToClipboard(context: Context, url: String) {
            val clipboardManager = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            clipboardManager.setPrimaryClip(ClipData.newPlainText("text", url))
            Toast.makeText(context, R.string.link_copied, Toast.LENGTH_SHORT).show()
        }

        fun openTelegram(activity: Activity) {
            try {
                activity.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://telegram.me/telegraph")))
                activity.finish()
            } catch (e: ActivityNotFoundException) {
                Toast.makeText(activity, activity.getString(R.string.error_app_not_found), Toast.LENGTH_SHORT).show()
            }
        }

        fun openPro(activity: Activity) {
            try {
                activity.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=com.telex.pro")))
            } catch (e: ActivityNotFoundException) {
                Toast.makeText(activity, activity.getString(R.string.error_app_not_found), Toast.LENGTH_SHORT).show()
            }
        }

        fun openUrl(context: Context, url: String?, onError: (String) -> Unit) {
            try {
                if (!url.isNullOrEmpty()) {
                    val intent =
                            if (url.startsWith("intent://")) {
                                val appIntent = Intent.parseUri(url, Intent.URI_INTENT_SCHEME)
                                val appPackage = appIntent.getPackage().orEmpty()
                                val existPackageIntent = context.packageManager.getLaunchIntentForPackage(appPackage)
                                if (existPackageIntent != null || appPackage.isEmpty()) {
                                    appIntent
                                } else {
                                    val marketIntent = Intent(ACTION_VIEW)
                                    marketIntent.data = Uri.parse("market://details?id=$appPackage")
                                    marketIntent
                                }
                            } else {
                                Intent(ACTION_VIEW, Uri.parse(url))
                            }

                    context.startActivity(intent)
                }
            } catch (ignored: ActivityNotFoundException) {
                onError.invoke(context.getString(R.string.error_app_not_found))
            }
        }
    }
}
