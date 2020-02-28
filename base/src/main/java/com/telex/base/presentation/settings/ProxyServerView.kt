package com.telex.base.presentation.settings

import com.telex.base.model.source.local.ProxyServer
import com.telex.base.presentation.base.BaseMvpView
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
