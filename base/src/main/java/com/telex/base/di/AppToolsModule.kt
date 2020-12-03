package com.telex.base.di

import android.content.Context
import com.telex.base.analytics.AnalyticsReporter
import com.telex.base.analytics.DefaultAnalyticsReporter
import com.telex.base.model.interactors.DefaultRemoteConfigInteractor
import com.telex.base.model.interactors.RemoteConfigInteractor
import com.telex.base.model.source.local.AppData
import com.telex.base.review.AppReviewManager
import com.telex.base.review.DefaultAppReviewManager
import toothpick.config.Module

class AppToolsModule(context: Context) : Module() {
    init {
        val appData = AppData(context)
        bind(AppData::class.java).toInstance(appData)

        bind(AnalyticsReporter::class.java).toInstance(DefaultAnalyticsReporter())
        bind(RemoteConfigInteractor::class.java).toInstance(DefaultRemoteConfigInteractor())
        bind(AppReviewManager::class.java).toInstance(DefaultAppReviewManager())
    }
}
