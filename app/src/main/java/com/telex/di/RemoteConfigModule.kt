package com.telex.di

import com.telex.base.model.interactors.RemoteConfigInteractor
import com.telex.model.interactors.FirebaseRemoteConfigInteractor
import toothpick.config.Module

class RemoteConfigModule : Module() {
    init {
        bind(RemoteConfigInteractor::class.java).toInstance(FirebaseRemoteConfigInteractor())
    }
}
