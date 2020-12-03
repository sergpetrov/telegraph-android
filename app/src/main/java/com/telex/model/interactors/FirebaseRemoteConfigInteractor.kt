package com.telex.model.interactors

import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import com.telex.R
import com.telex.base.model.interactors.RemoteConfigInteractor
import com.telex.base.model.interactors.RemoteConfigInteractor.Companion.CREATED_WITH_CAPTION_DISABLED
import com.telex.base.model.interactors.RemoteConfigInteractor.Companion.MINIMUM_FETCH_INTERVAL_IN_SECONDS
import com.telex.base.model.interactors.RemoteConfigInteractor.Companion.TOP_BANNER
import com.telex.base.model.source.remote.data.TopBannerData
import com.telex.base.utils.Constants.isDebug
import timber.log.Timber

/**
 * @author Sergey Petrov
 */
class FirebaseRemoteConfigInteractor : RemoteConfigInteractor {
    private var remoteConfig = FirebaseRemoteConfig.getInstance()

    private val minimumFetchIntervalInSeconds = when {
        isDebug() -> 0
        else -> MINIMUM_FETCH_INTERVAL_IN_SECONDS
    }

    init {
        val configSettings = FirebaseRemoteConfigSettings.Builder()
                .setMinimumFetchIntervalInSeconds(minimumFetchIntervalInSeconds)
                .build()
        remoteConfig.setConfigSettingsAsync(configSettings)
        remoteConfig.setDefaultsAsync(R.xml.remote_config_defaults)
    }

    override fun fetch(onCompleted: () -> Unit) {
        remoteConfig.fetchAndActivate()
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
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
