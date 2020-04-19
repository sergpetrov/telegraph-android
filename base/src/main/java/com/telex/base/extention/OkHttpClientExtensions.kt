package com.telex.base.extention

import okhttp3.OkHttpClient
import timber.log.Timber
import java.security.SecureRandom
import java.security.cert.CertificateException
import java.security.cert.X509Certificate
import java.util.concurrent.TimeUnit
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager

/**
 * @author Sergey Petrov
 */
fun OkHttpClient.Builder.withDefaults(): OkHttpClient.Builder {
    readTimeout(30, TimeUnit.SECONDS)
    connectTimeout(30, TimeUnit.SECONDS)
    writeTimeout(60, TimeUnit.SECONDS)
    return this
}

fun OkHttpClient.Builder.ignoreSSLCertificateException(): OkHttpClient.Builder {
    try {
        val trustAllCerts = arrayOf<TrustManager>(object : X509TrustManager {
            override fun getAcceptedIssuers(): Array<X509Certificate> = arrayOf()

            @Throws(CertificateException::class)
            override fun checkClientTrusted(chain: Array<X509Certificate>, authType: String) {
            }

            @Throws(CertificateException::class)
            override fun checkServerTrusted(chain: Array<X509Certificate>, authType: String) {
            }
        })

        val sslContext = SSLContext.getInstance("SSL").apply {
            init(null, trustAllCerts, SecureRandom())
        }
        sslSocketFactory(sslContext.socketFactory, trustAllCerts[0] as X509TrustManager)
        hostnameVerifier { _, _ -> true }
    } catch (error: Exception) {
        Timber.e(error)
    }
    return this
}
