package com.telex.base.presentation.page.dialogs

import com.telex.base.presentation.base.BaseMvpView
import moxy.viewstate.strategy.AddToEndSingleStrategy
import moxy.viewstate.strategy.StateStrategyType

/**
 * @author Sergey Petrov
 */
@StateStrategyType(AddToEndSingleStrategy::class)
interface AuthorView : BaseMvpView {
    fun showAuthor(name: String?, url: String?)
}
