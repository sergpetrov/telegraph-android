package com.telex.base.model.interactors

import com.telex.base.model.source.remote.data.TopBannerData
import java.util.concurrent.TimeUnit

/**
 * @author Sergey Petrov
 */
interface RemoteConfigInteractor {
    fun fetch(onCompleted: () -> Unit)
    fun getTopBanner(): TopBannerData?
    fun createdWithCaptionDisabled(): Boolean

    companion object {
        val MINIMUM_FETCH_INTERVAL_IN_SECONDS = TimeUnit.HOURS.toSeconds(12)
        const val TOP_BANNER = "top_banner"
        const val CREATED_WITH_CAPTION_DISABLED = "created_with_caption_disabled"
    }
}
