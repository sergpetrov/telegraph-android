package com.telex.base.presentation.base

import androidx.annotation.StringRes
import moxy.MvpView
import moxy.viewstate.strategy.AddToEndSingleStrategy
import moxy.viewstate.strategy.OneExecutionStateStrategy
import moxy.viewstate.strategy.StateStrategyType

/**
 * @author Sergey Petrov
 */
@StateStrategyType(AddToEndSingleStrategy::class)
interface BaseMvpView : MvpView {
    @StateStrategyType(OneExecutionStateStrategy::class)
    fun showError(message: String)
    @StateStrategyType(OneExecutionStateStrategy::class)
    fun showError(@StringRes resourceId: Int)
}
