package com.telex.base.model.source.remote.interceptor

import com.telex.base.model.system.ServerManager
import com.telex.base.utils.ServerConfig
import okhttp3.Interceptor
import okhttp3.Response

/**
 * @author Sergey Petrov
 */
class GlideInterceptor(private val serverManager: ServerManager) : Interceptor {

    private val wrongTelegraphServer = "a-telegraph.stel.com"

    override fun intercept(chain: Interceptor.Chain): Response {
        var request = chain.request()
        if ((request.url().host() == ServerConfig.Telegraph.server || request.url().host() == wrongTelegraphServer) &&
                !serverManager.isUserProxyServerEnabled() &&
                serverManager.getCurrentServerConfig() != ServerConfig.Telegraph
        ) {
            request = request
                    .newBuilder()
                    .apply {
                        if (request.url().toString().contains(wrongTelegraphServer)) {
                            url(request.url().toString().replace(wrongTelegraphServer, serverManager.getCurrentServerConfig().server))
                        } else {
                            url(request.url().toString().replace(ServerConfig.Telegraph.server, serverManager.getCurrentServerConfig().server))
                        }
                    }
                    .build()
        }
        return chain.proceed(request)
    }
}
