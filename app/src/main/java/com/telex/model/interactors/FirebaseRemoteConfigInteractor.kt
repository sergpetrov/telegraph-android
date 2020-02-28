package com.telex.model.interactors

import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import com.telex.BuildConfig
import com.telex.R
import com.telex.base.model.interactors.RemoteConfigInteractor
import com.telex.base.model.interactors.RemoteConfigInteractor.Companion.CACHE_EXPIRATION
import com.telex.base.model.interactors.RemoteConfigInteractor.Companion.CREATED_WITH_CAPTION_DISABLED
import com.telex.base.model.interactors.RemoteConfigInteractor.Companion.TOP_BANNER
import com.telex.base.model.source.remote.data.TopBannerData
import timber.log.Timber

/**
 * @author Sergey Petrov
 */
class FirebaseRemoteConfigInteractor : RemoteConfigInteractor {
    private var remoteConfig = FirebaseRemoteConfig.getInstance()

    init {
        val configSettings = FirebaseRemoteConfigSettings.Builder()
                .setDeveloperModeEnabled(BuildConfig.DEBUG)
                .build()
        remoteConfig.setConfigSettings(configSettings)
        remoteConfig.setDefaults(R.xml.remote_config_defaults)
    }

    override fun fetch(onCompleted: () -> Unit) {
        val cacheExpiration = if (BuildConfig.DEBUG) 0 else CACHE_EXPIRATION

        remoteConfig.fetch(cacheExpiration)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        remoteConfig.activateFetched()
                        onCompleted.invoke()
                    }
                }
    }

    override fun getTopBanner(): TopBannerData? {
        val json = remoteConfig.getString(TOP_BANNER)
        if (json.isNotEmpty()) {
            try {
                return Gson().fromJson(json, TopBannerData::class.java)
            } catch (exception: JsonSyntaxException) {
                Timber.e(exception)
            }
        }
        return null
    }

    override fun createdWithCaptionDisabled(): Boolean {
        return remoteConfig.getBoolean(CREATED_WITH_CAPTION_DISABLED)
    }
}
