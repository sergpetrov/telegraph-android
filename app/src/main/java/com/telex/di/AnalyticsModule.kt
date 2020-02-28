package com.telex.di

import com.telex.analytics.FirebaseAnalyticsReporter
import com.telex.base.analytics.AnalyticsReporter
import toothpick.config.Module

class AnalyticsModule : Module() {
    init {
        bind(AnalyticsReporter::class.java).toInstance(FirebaseAnalyticsReporter())
    }
}
