package com.telex.base

import androidx.appcompat.app.AppCompatDelegate
import androidx.multidex.MultiDexApplication
import com.telex.base.di.AppModule
import com.telex.base.di.AppToolsModule
import com.telex.base.di.Scopes
import com.telex.base.model.interactors.RemoteConfigInteractor
import com.telex.base.model.source.local.AppData
import com.telex.base.model.system.ServerManager
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
                AppToolsModule(this)
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
