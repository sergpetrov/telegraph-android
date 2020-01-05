package com.telex.di

import com.telex.analytics.AnalyticsReporter
import com.telex.analytics.FirebaseAnalyticsReporter
import toothpick.config.Module

class AnalyticsModule : Module() {
    init {
        bind(AnalyticsReporter::class.java).toInstance(FirebaseAnalyticsReporter())
    }
}
