package com.telex.base.model.interactors

import com.telex.base.model.source.remote.data.TopBannerData

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
