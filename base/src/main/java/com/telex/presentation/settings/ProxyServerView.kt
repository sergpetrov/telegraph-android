package com.telex.presentation.settings

import com.telex.model.source.local.ProxyServer
import com.telex.presentation.base.BaseMvpView
import moxy.viewstate.strategy.AddToEndSingleStrategy
import moxy.viewstate.strategy.StateStrategyType

/**
 * @author Sergey Petrov
 */
@StateStrategyType(AddToEndSingleStrategy::class)
interface ProxyServerView : BaseMvpView {
    fun showProxyServer(proxy: ProxyServer)
    fun onProxyServerSaved()
    fun showInvalidServerError()
    fun hideInvalidServerError()
    fun showInvalidPortError()
    fun hideInvalidPortError()
}
