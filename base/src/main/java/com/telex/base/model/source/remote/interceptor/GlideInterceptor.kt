package com.telex.base.model.source.remote.interceptor

import com.telex.base.model.system.ServerManager
import com.telex.base.utils.ServerConfig
import okhttp3.Interceptor
import okhttp3.Response

/**
 * @author Sergey Petrov
 */
class GlideInterceptor(private val serverManager: ServerManager) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        var request = chain.request()
        if ((request.url().host() == ServerConfig.Telegraph.server) &&
                !serverManager.isUserProxyServerEnabled() &&
                serverManager.getCurrentServerConfig() != ServerConfig.Telegraph
        ) {
            request = request
                    .newBuilder()
                    .url(request.url().toString().replace(ServerConfig.Telegraph.server, serverManager.getCurrentServerConfig().server))
                    .build()
        }
        return chain.proceed(request)
    }
}
