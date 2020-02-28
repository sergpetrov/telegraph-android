package com.telex.base.model.system

import android.content.Context
import com.bumptech.glide.Glide
import com.bumptech.glide.Registry
import com.bumptech.glide.annotation.Excludes
import com.bumptech.glide.annotation.GlideModule
import com.bumptech.glide.integration.okhttp3.OkHttpLibraryGlideModule
import com.bumptech.glide.integration.okhttp3.OkHttpUrlLoader
import com.bumptech.glide.load.model.GlideUrl
import com.bumptech.glide.module.AppGlideModule
import com.telex.base.di.Scopes
import java.io.InputStream
import toothpick.Toothpick

@Excludes(OkHttpLibraryGlideModule::class)
@GlideModule
class GlideModule : AppGlideModule() {

    private val serverManager: ServerManager by lazy {
        Toothpick.openScope(Scopes.App)
                .getInstance(ServerManager::class.java)
    }

    override fun registerComponents(context: Context, glide: Glide, registry: Registry) {
        val factory = OkHttpUrlLoader.Factory(serverManager.getGlideOkHttpClient())
        glide.registry.replace(GlideUrl::class.java, InputStream::class.java, factory)
    }

    override fun isManifestParsingEnabled() = false
}
