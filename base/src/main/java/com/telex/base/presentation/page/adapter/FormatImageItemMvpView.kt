package com.telex.base.presentation.page.adapter

import com.telex.base.presentation.base.BaseMvpView
import com.telex.base.presentation.page.format.ImageFormat
import moxy.viewstate.strategy.OneExecutionStateStrategy
import moxy.viewstate.strategy.StateStrategyType

/**
 * @author Sergey Petrov
 */
interface FormatImageItemMvpView : BaseMvpView {
    @StateStrategyType(OneExecutionStateStrategy::class)
    fun updateImage(format: ImageFormat)
}
