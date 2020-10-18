package com.telex.analytics

import android.content.Context
import android.os.Bundle
import com.google.firebase.analytics.FirebaseAnalytics
import com.telex.base.analytics.AnalyticsReporter

/**
 * @author Sergey Petrov
 */
class FirebaseAnalyticsReporter(
        private val context: Context
) : AnalyticsReporter {

    override fun logEvent(eventKey: String) {
        FirebaseAnalytics.getInstance(context).logEvent(eventKey, Bundle())
    }
}
