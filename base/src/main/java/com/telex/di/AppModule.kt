package com.telex.di

import android.content.Context
import android.content.res.Resources
import com.telex.model.source.local.TelegraphDatabase
import com.telex.model.source.remote.api.RestApiProvider
import com.telex.model.system.ServerManager
import id.zelory.compressor.Compressor
import toothpick.config.Module

class AppModule(context: Context) : Module() {
    init {
        bind(Context::class.java).toInstance(context)
        bind(Resources::class.java).toInstance(context.resources)
        bind(ServerManager::class.java).singleton()
        bind(RestApiProvider::class.java).singleton()
        bind(TelegraphDatabase::class.java).toInstance(TelegraphDatabase.getInstance(context))
        bind(Compressor::class.java).toInstance(Compressor(context))
    }
}
