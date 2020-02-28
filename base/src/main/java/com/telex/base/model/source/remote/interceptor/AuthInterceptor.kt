package com.telex.base.model.source.remote.interceptor

import com.telex.base.model.source.local.AppData
import okhttp3.Interceptor
import okhttp3.Response

/**
 * @author Sergey Petrov
 */
class AuthInterceptor(private val appData: AppData) : Interceptor {
    val HEADER_SET_COOKIE = "Set-Cookie"

    override fun intercept(chain: Interceptor.Chain): Response {
        var request = chain.request()
        appData.getCurrentAccessToken()?.let {
            val url = request.url().newBuilder()
                    .addEncodedQueryParameter("access_token", appData.getCurrentAccessToken())
                    .build()

            request = chain.request().newBuilder()
                    .url(url).build()
        }

        val response = chain.proceed(request)

        val cookies = response.headers(HEADER_SET_COOKIE)
        if (cookies.isNotEmpty() && cookies.size > 1) {
            val accessToken = cookies[1].split(";")[0].split("=")[1]
            appData.putCurrentAccessToken(accessToken)
        }
        return response
    }
}
