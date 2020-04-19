package com.telex.base.presentation.home

import com.telex.base.model.source.remote.data.TopBannerData
import com.telex.base.presentation.base.BaseActivity
import com.telex.base.presentation.pages.BaseTopBannerDelegate

/**
 * @author Sergey Petrov
 */
class TopBannerDelegate(
    activity: BaseActivity,
    banner: TopBannerData
) : BaseTopBannerDelegate(activity, banner) {

    override val enabled: Boolean
        get() = !banner.disabled && banner.showForPro

    override fun showDefaultOnSecondActionClicked() {
    }
}
