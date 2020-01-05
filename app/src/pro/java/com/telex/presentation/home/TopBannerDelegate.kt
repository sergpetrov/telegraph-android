package com.telex.presentation.home

import com.telex.model.source.remote.data.TopBannerData
import com.telex.presentation.base.BaseActivity

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
