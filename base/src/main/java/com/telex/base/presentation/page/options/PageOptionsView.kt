package com.telex.base.presentation.page.options

import com.telex.base.presentation.base.BaseMvpView
import moxy.viewstate.strategy.AddToEndSingleStrategy
import moxy.viewstate.strategy.StateStrategyType

/**
 * @author Sergey Petrov
 */
@StateStrategyType(AddToEndSingleStrategy::class)
interface PageOptionsView : BaseMvpView {
    fun showProgress(isVisible: Boolean)
}
