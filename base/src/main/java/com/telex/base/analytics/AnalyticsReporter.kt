package com.telex.base.analytics

/**
 * @author Sergey Petrov
 */
interface AnalyticsReporter {
    fun logEvent(eventKey: String)
}
