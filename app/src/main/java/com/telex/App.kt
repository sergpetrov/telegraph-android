package com.telex

import com.telex.base.BaseApp
import com.telex.base.di.AppModule
import com.telex.base.di.Scopes
import com.telex.base.utils.Constants
import com.telex.di.AnalyticsModule
import com.telex.di.RemoteConfigModule
import com.telex.utils.DebugTree
import com.telex.utils.ReleaseTree
import timber.log.Timber
import toothpick.Scope
import toothpick.Toothpick

/**
 * @author Sergey Petrov
 */
class App : BaseApp() {

    override fun buildToothpickScope(): Scope? {
        val scope = Toothpick.openScope(Scopes.App)
        scope.installModules(
                AppModule(this),
                AnalyticsModule(this),
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
}
