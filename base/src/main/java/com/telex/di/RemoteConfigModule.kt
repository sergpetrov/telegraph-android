package com.telex.di

import com.telex.model.interactors.DefaultRemoteConfigInteractor
import com.telex.model.interactors.RemoteConfigInteractor
import toothpick.config.Module

class RemoteConfigModule : Module() {
    init {
        bind(RemoteConfigInteractor::class.java).toInstance(DefaultRemoteConfigInteractor())
    }
}
