package com.telex.model.interactors

import com.telex.model.source.remote.data.TopBannerData

/**
 * @author Sergey Petrov
 */
interface RemoteConfigInteractor {
    fun fetch(onCompleted: () -> Unit)
    fun getTopBanner(): TopBannerData?
    fun createdWithCaptionDisabled(): Boolean

    companion object {
        const val CACHE_EXPIRATION = 3600L // 1 hour in milliseconds
        const val TOP_BANNER = "top_banner"
        const val CREATED_WITH_CAPTION_DISABLED = "created_with_caption_disabled"
    }
}
