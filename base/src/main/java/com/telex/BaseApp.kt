package com.telex

import androidx.appcompat.app.AppCompatDelegate
import androidx.multidex.MultiDexApplication
import com.telex.di.AnalyticsModule
import com.telex.di.AppModule
import com.telex.di.RemoteConfigModule
import com.telex.di.Scopes
import com.telex.model.interactors.RemoteConfigInteractor
import com.telex.model.source.local.AppData
import com.telex.model.system.ServerManager
import javax.inject.Inject
import timber.log.Timber
import toothpick.Scope
import toothpick.Toothpick
import toothpick.configuration.Configuration

/**
 * @author Sergey Petrov
 */
abstract class BaseApp : MultiDexApplication() {

    @Inject
    lateinit var appData: AppData
    @Inject
    lateinit var serverManager: ServerManager
    @Inject
    lateinit var remoteConfigInteractor: RemoteConfigInteractor

    init {
        instance = this
    }

    override fun onCreate() {
        super.onCreate()

        setupToothpick()
        setupTimber()
        setupNightMode()
        setupRemoteConfig()
    }

    private fun setupToothpick() {
        if (BuildConfig.DEBUG) {
            Toothpick.setConfiguration(Configuration.forDevelopment().preventMultipleRootScopes())
        } else {
            Toothpick.setConfiguration(Configuration.forProduction())
        }
        Toothpick.inject(this, buildToothpickScope())
    }

    protected open fun buildToothpickScope(): Scope? {
        val scope = Toothpick.openScope(Scopes.App)
        scope.installModules(
                AppModule(this),
                AnalyticsModule(),
                RemoteConfigModule()
        )
        return scope
    }

    protected open fun setupTimber() {
        Timber.plant(Timber.DebugTree())
    }

    private fun setupNightMode() {
        val mode =
                if (appData.isNightModeEnabled()) {
                    AppCompatDelegate.MODE_NIGHT_YES
                } else {
                    AppCompatDelegate.MODE_NIGHT_NO
                }

        AppCompatDelegate.setDefaultNightMode(mode)
    }

    private fun setupRemoteConfig() {
        remoteConfigInteractor.fetch {}
    }

    companion object {
        lateinit var instance: BaseApp
    }
}
