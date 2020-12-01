package com.telex.di

import android.content.Context
import com.telex.analytics.FirebaseAnalyticsReporter
import com.telex.base.analytics.AnalyticsReporter
import com.telex.base.model.interactors.RemoteConfigInteractor
import com.telex.base.model.source.local.AppData
import com.telex.base.review.AppReviewManager
import com.telex.model.interactors.FirebaseRemoteConfigInteractor
import com.telex.review.InAppReviewManager
import toothpick.config.Module

class AppToolsModule(context: Context) : Module() {
    init {
        val appData = AppData(context)
        bind(AppData::class.java).toInstance(appData)

        val analyticsReporter = FirebaseAnalyticsReporter()
        bind(AnalyticsReporter::class.java).toInstance(analyticsReporter)
        bind(RemoteConfigInteractor::class.java).toInstance(FirebaseRemoteConfigInteractor())

        val appReviewManager = InAppReviewManager(context, appData)
        bind(AppReviewManager::class.java).toInstance(appReviewManager)
    }
}
