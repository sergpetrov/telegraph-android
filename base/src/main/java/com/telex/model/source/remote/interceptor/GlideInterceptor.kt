package com.telex.model.source.remote.interceptor

import com.telex.model.system.ServerManager
import com.telex.utils.Constants
import okhttp3.Interceptor
import okhttp3.Response

/**
 * @author Sergey Petrov
 */
class GlideInterceptor(private val serverManager: ServerManager) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        var request = chain.request()
        if (request.url().host() == Constants.telegraphServer &&
                !serverManager.isUserProxyServerEnabled() &&
                serverManager.getCurrentServer() == Constants.graphServer
        ) {
            request = request
                    .newBuilder()
                    .url(request.url().toString().replace(Constants.telegraphServer, Constants.graphServer))
                    .build()
        }
        return chain.proceed(request)
    }
}
