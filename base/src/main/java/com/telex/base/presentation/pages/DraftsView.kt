package com.telex.base.presentation.pages

import com.telex.base.model.source.local.entity.Page
import com.telex.base.presentation.base.BaseMvpView
import moxy.viewstate.strategy.AddToEndSingleStrategy
import moxy.viewstate.strategy.StateStrategyType

/**
 * @author Sergey Petrov
 */
@StateStrategyType(AddToEndSingleStrategy::class)
interface DraftsView : BaseMvpView {
    fun showPages(pages: List<Page>, hasMore: Boolean)
    fun showEmpty()
    fun hideEmpty()
}
