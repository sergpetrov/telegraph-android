package com.telex.base.di

import com.telex.base.analytics.AnalyticsReporter
import com.telex.base.analytics.DefaultAnalyticsReporter
import toothpick.config.Module

class AnalyticsModule : Module() {
    init {
        bind(AnalyticsReporter::class.java).toInstance(DefaultAnalyticsReporter())
    }
}
