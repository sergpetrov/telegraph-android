package com.telex.presentation.home

import com.telex.model.source.local.entity.Page
import com.telex.model.source.remote.data.TopBannerData
import com.telex.presentation.base.BaseMvpView
import moxy.viewstate.strategy.AddToEndSingleStrategy
import moxy.viewstate.strategy.StateStrategyType

/**
 * @author Sergey Petrov
 */
@StateStrategyType(AddToEndSingleStrategy::class)
interface PagesView : BaseMvpView {
    fun showProgress(isVisible: Boolean)
    fun showPages(pages: List<Page>, hasMore: Boolean)
    fun showEmpty()
    fun hideEmpty()
    fun showAdapterProgress()
    fun hideAdapterProgress()
    fun showTopBanner(banner: TopBannerData)
    fun hideTopBanner()
}
