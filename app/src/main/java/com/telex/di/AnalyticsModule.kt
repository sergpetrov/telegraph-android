package com.telex.di

import android.content.Context
import com.telex.analytics.FirebaseAnalyticsReporter
import com.telex.base.analytics.AnalyticsReporter
import toothpick.config.Module

class AnalyticsModule(context: Context) : Module() {
    init {
        bind(AnalyticsReporter::class.java).toInstance(FirebaseAnalyticsReporter(context))
    }
}
