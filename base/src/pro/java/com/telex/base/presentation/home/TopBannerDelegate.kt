package com.telex.base.presentation.home

import com.telex.base.model.source.remote.data.TopBannerData
import com.telex.base.presentation.pages.BaseTopBannerDelegate
import com.telex.base.presentation.pages.PagesFragment

/**
 * @author Sergey Petrov
 */
class TopBannerDelegate(
    fragment: PagesFragment?,
    banner: TopBannerData
) : BaseTopBannerDelegate(fragment, banner) {

    override val enabled: Boolean
        get() = !banner.disabled && banner.showForPro

    override fun showDefaultOnSecondActionClicked() {
    }
}
