package com.telex

import com.crashlytics.android.Crashlytics
import com.crashlytics.android.answers.Answers
import com.crashlytics.android.core.CrashlyticsCore
import com.telex.di.AnalyticsModule
import com.telex.di.AppModule
import com.telex.di.RemoteConfigModule
import com.telex.di.Scopes
import com.telex.utils.Constants
import com.telex.utils.DebugTree
import com.telex.utils.ReleaseTree
import io.fabric.sdk.android.Fabric
import timber.log.Timber
import toothpick.Scope
import toothpick.Toothpick

/**
 * @author Sergey Petrov
 */
class App : BaseApp() {

    override fun onCreate() {
        super.onCreate()
        setupFabric()
    }

    override fun buildToothpickScope(): Scope? {
        val scope = Toothpick.openScope(Scopes.App)
        scope.installModules(
                AppModule(this),
                AnalyticsModule(),
                RemoteConfigModule()
        )
        return scope
    }

    override fun setupTimber() {
        if (Constants.isRelease()) {
            Timber.plant(ReleaseTree())
        } else {
            Timber.plant(DebugTree())
        }
    }

    private fun setupFabric() {
        val crashlyticsCore = CrashlyticsCore.Builder().disabled(BuildConfig.DEBUG).build()
        Fabric.with(this, Crashlytics.Builder()
                .core(crashlyticsCore)
                .answers(Answers())
                .build()
        )
    }
}
