package com.telex.analytics

/**
 * @author Sergey Petrov
 */
interface AnalyticsReporter {
    fun logEvent(eventKey: String)
}
