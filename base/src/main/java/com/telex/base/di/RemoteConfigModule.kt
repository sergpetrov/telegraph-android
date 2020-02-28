package com.telex.base.di

import com.telex.base.model.interactors.DefaultRemoteConfigInteractor
import com.telex.base.model.interactors.RemoteConfigInteractor
import toothpick.config.Module

class RemoteConfigModule : Module() {
    init {
        bind(RemoteConfigInteractor::class.java).toInstance(DefaultRemoteConfigInteractor())
    }
}
