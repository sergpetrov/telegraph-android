package com.telex.analytics

import android.os.Bundle
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.ktx.Firebase
import com.telex.base.analytics.AnalyticsReporter

/**
 * @author Sergey Petrov
 */
class FirebaseAnalyticsReporter : AnalyticsReporter {

    private val firebaseAnalytics = Firebase.analytics

    override fun logEvent(eventKey: String) {
        firebaseAnalytics.logEvent(eventKey, Bundle())
    }
}
