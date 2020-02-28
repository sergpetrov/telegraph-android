package com.telex.base.presentation.settings

import com.telex.base.analytics.AnalyticsHelper
import com.telex.base.model.source.local.ProxyServer
import com.telex.base.model.system.ServerManager
import com.telex.base.presentation.base.BasePresenter
import com.telex.base.presentation.base.ErrorHandler
import javax.inject.Inject
import moxy.InjectViewState

/**
 * @author Sergey Petrov
 */
@InjectViewState
class ProxyServerPresenter @Inject constructor(
    private val serverManager: ServerManager,
    errorHandler: ErrorHandler
) : BasePresenter<ProxyServerView>(errorHandler) {

    override fun onFirstViewAttach() {
        super.onFirstViewAttach()

        val proxy = serverManager.getUserProxyServer()

        if (proxy != null) {
            viewState.showProxyServer(proxy)
        }
    }

    fun saveProxyServer(type: ProxyServer.Type, host: String, port: String, user: String?, password: String?) {
        var isValid = true

        if (host.isEmpty()) {
            isValid = false
            viewState.showInvalidServerError()
        } else {
            viewState.hideInvalidServerError()
        }

        if (port.isEmpty()) {
            isValid = false
            viewState.showInvalidPortError()
        } else {
            try {
                checkPort(Integer.parseInt(port))
                viewState.hideInvalidPortError()
            } catch (e: IllegalArgumentException) {
                isValid = false
                viewState.showInvalidPortError()
            }
        }

        if (isValid) {
            AnalyticsHelper.logSaveProxy()

            serverManager.saveUserProxyServer(ProxyServer(type, host, port.toInt(), user, password, enabled = true))
            viewState.onProxyServerSaved()
        }
    }

    fun deleteProxyServer() {
        serverManager.deleteProxyServer()
        viewState.onProxyServerSaved()
    }

    // copy of java.net.InetSocketAddress#checkPort
    private fun checkPort(port: Int): Int {
        if (port < 0 || port > 0xFFFF)
            throw IllegalArgumentException("port out of range:$port")
        return port
    }
}
