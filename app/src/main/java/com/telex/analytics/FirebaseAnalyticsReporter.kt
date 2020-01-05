package com.telex.analytics

import com.crashlytics.android.answers.Answers
import com.crashlytics.android.answers.CustomEvent

/**
 * @author Sergey Petrov
 */
class FirebaseAnalyticsReporter : AnalyticsReporter {

    override fun logEvent(eventKey: String) {
        Answers.getInstance().logCustom(CustomEvent(eventKey))
    }
}
