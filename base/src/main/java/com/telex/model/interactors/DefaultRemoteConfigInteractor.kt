package com.telex.model.interactors

import com.telex.model.source.remote.data.TopBannerData

/**
 * @author Sergey Petrov
 */
class DefaultRemoteConfigInteractor : RemoteConfigInteractor {

    override fun fetch(onCompleted: () -> Unit) {
    }

    override fun getTopBanner(): TopBannerData? {
        return null
    }

    override fun createdWithCaptionDisabled(): Boolean {
        return true
    }
}
