package com.telex.base.presentation.drawer

import com.telex.base.model.source.local.entity.User
import com.telex.base.presentation.base.BaseMvpView
import moxy.viewstate.strategy.AddToEndSingleStrategy
import moxy.viewstate.strategy.OneExecutionStateStrategy
import moxy.viewstate.strategy.StateStrategyType

/**
 * @author Sergey Petrov
 */
@StateStrategyType(AddToEndSingleStrategy::class)
interface BottomNavigationDrawerView : BaseMvpView {
    fun showCurrentAccount(user: User)
    fun showAccounts(users: List<User>)
    fun showDraftsCount(count: Int)
    @StateStrategyType(OneExecutionStateStrategy::class)
    fun showProxyServerEnabled()
    @StateStrategyType(OneExecutionStateStrategy::class)
    fun showProxyServerDisabled()
    @StateStrategyType(OneExecutionStateStrategy::class)
    fun showProxyServerNotExist()
    @StateStrategyType(OneExecutionStateStrategy::class)
    fun updateNightMode(nightModeEnabled: Boolean, needRecreate: Boolean)
}
