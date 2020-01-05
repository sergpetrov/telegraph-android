package com.telex.presentation.page.dialogs

import com.telex.presentation.base.BaseMvpView
import moxy.viewstate.strategy.AddToEndSingleStrategy
import moxy.viewstate.strategy.StateStrategyType

/**
 * @author Sergey Petrov
 */
@StateStrategyType(AddToEndSingleStrategy::class)
interface AuthorView : BaseMvpView {
    fun showProgress(isVisible: Boolean)
    fun showAuthor(name: String?, url: String?)
}
