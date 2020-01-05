package com.telex.presentation.settings

import com.telex.model.source.local.entity.User
import com.telex.presentation.base.BaseMvpView
import moxy.viewstate.strategy.AddToEndSingleStrategy
import moxy.viewstate.strategy.StateStrategyType

/**
 * @author Sergey Petrov
 */
@StateStrategyType(AddToEndSingleStrategy::class)
interface AccountSettingsView : BaseMvpView {
    fun showProgress(isVisible: Boolean)
    fun showUser(user: User?)
    fun onUserSaved()
    fun onLogout()
}
