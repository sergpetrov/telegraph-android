package com.telex.base.presentation.settings.account

import com.telex.base.model.source.local.entity.User
import com.telex.base.presentation.base.BaseMvpView
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
