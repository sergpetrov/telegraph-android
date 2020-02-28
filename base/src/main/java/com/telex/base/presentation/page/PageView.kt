package com.telex.base.presentation.page

import com.telex.base.model.source.local.entity.Page
import com.telex.base.presentation.base.BaseMvpView
import com.telex.base.presentation.page.format.Format
import moxy.viewstate.strategy.AddToEndSingleStrategy
import moxy.viewstate.strategy.StateStrategyType

/**
 * @author Sergey Petrov
 */
@StateStrategyType(AddToEndSingleStrategy::class)
interface PageView : BaseMvpView {
    fun showProgress(isVisible: Boolean)
    fun showPage(page: Page, formats: List<Format>)
    fun onPageSaved()
    fun showMore(page: Page)
}
