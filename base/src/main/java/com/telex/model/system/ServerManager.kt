package com.telex.model.system

import android.content.Context
import com.bumptech.glide.Glide
import com.bumptech.glide.integration.okhttp3.OkHttpUrlLoader
import com.bumptech.glide.load.model.GlideUrl
import com.google.gson.Gson
import com.telex.BuildConfig
import com.telex.exceptions.ProxyConnectionException
import com.telex.exceptions.TelegraphUnavailableException
import com.telex.extention.ignoreSSLCertificateException
import com.telex.extention.withDefaults
import com.telex.extention.withProxy
import com.telex.model.source.local.AppData
import com.telex.model.source.local.ProxyServer
import com.telex.model.source.remote.interceptor.AuthInterceptor
import com.telex.model.source.remote.interceptor.ErrorsInterceptor
import com.telex.model.source.remote.interceptor.GlideInterceptor
import com.telex.utils.ServerConfig
import io.reactivex.Completable
import java.io.IOException
import java.io.InputStream
import java.net.Authenticator
import java.net.HttpURLConnection
import java.net.InetSocketAddress
import java.net.PasswordAuthentication
import java.net.Proxy
import java.net.URL
import javax.inject.Inject
import javax.inject.Singleton
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import timber.log.Timber

/**
 * @author Sergey Petrov
 */
@Singleton
class ServerManager @Inject constructor(
    private val context: Context,
    private val appData: AppData
) {

    private val gson = Gson()
    var isServerConfigChanged = false

    init {
        endPoint = getEndPoint()
    }

    fun getApiEndPoint(): String {
        return getCurrentServerConfig().apiEndPoint
    }

    fun getEndPoint(): String {
        return getCurrentServerConfig().endPoint
    }

    fun getImageUploadEndPoint(): String {
        return getCurrentServerConfig().imageUploadEndPoint
    }

    fun getCurrentServerConfig(): ServerConfig {
        return ServerConfig.getByServer(appData.getServer())
    }

    fun checkAvailableServer(): Completable {
        return Completable.fromCallable {
            val server = findAvailableServer()
            if (server != null) {
                appData.putServer(server)
                endPoint = getEndPoint()
                isServerConfigChanged = true
            } else {
                if (isUserProxyServerEnabled()) {
                    throw ProxyConnectionException()
                } else {
                    throw TelegraphUnavailableException()
                }
            }
        }
    }

    private fun findAvailableServer(): String? {
        var connection: HttpURLConnection? = null

        ServerConfig.values().forEach { config ->
            try {
                val userProxyServer = getUserProxyServer()
                val proxyServer = if (userProxyServer != null && userProxyServer.enabled) userProxyServer else null
                val proxy =
                        if (proxyServer != null) {
                            Authenticator.setDefault(object : Authenticator() {
                                override fun getPasswordAuthentication(): PasswordAuthentication {
                                    return PasswordAuthentication(proxyServer.user, proxyServer.password?.toCharArray())
                                }
                            })
                            Proxy(Proxy.Type.valueOf(proxyServer.type.name), InetSocketAddress.createUnresolved(proxyServer.host, proxyServer.port))
                        } else null

                val url = URL(config.endPoint)
                connection = if (proxy != null) {
                    url.openConnection(proxy)
                } else {
                    url.openConnection()
                } as HttpURLConnection

                connection?.connectTimeout = 2000
                connection?.connect()
                return config.server
            } catch (error: IOException) {
                Timber.d(error)
            } finally {
                connection?.disconnect()
            }
        }
        return null
    }

    fun isUserProxyServerEnabled(): Boolean {
        return getUserProxyServer()?.enabled == true
    }

    fun getUserProxyServer(): ProxyServer? {
        return convertProxyServer(appData.getUserProxyServer())
    }

    private fun convertProxyServer(json: String?): ProxyServer? {
        return if (!json.isNullOrEmpty()) {
            Gson().fromJson(json, ProxyServer::class.java)
        } else null
    }

    fun deleteProxyServer() {
        appData.putUserProxyServer(null)
        isServerConfigChanged = true
        changeGlideOkHttpClient()
    }

    fun saveUserProxyServer(proxyServer: ProxyServer) {
        appData.putUserProxyServer(gson.toJson(proxyServer))
        isServerConfigChanged = true
        changeGlideOkHttpClient()
    }

    fun enableUserProxyServer(enable: Boolean) {
        val proxyServer = getUserProxyServer()
        proxyServer?.enabled = enable
        if (proxyServer != null) {
            appData.putUserProxyServer(gson.toJson(proxyServer))
        }
        isServerConfigChanged = true
        changeGlideOkHttpClient()
    }

    fun getOkHttpClient(): OkHttpClient {
        return with(OkHttpClient.Builder()) {
            withDefaults()
            withProxy(getUserProxyServer())

            if (BuildConfig.DEBUG) {
                val httpLoggingInterceptor = HttpLoggingInterceptor()
                httpLoggingInterceptor.level = HttpLoggingInterceptor.Level.BODY
                addInterceptor(httpLoggingInterceptor)
            }
            addInterceptor(ErrorsInterceptor())
            addNetworkInterceptor(AuthInterceptor(appData))
            ignoreSSLCertificateException()
            build()
        }
    }

    fun getGlideOkHttpClient(): OkHttpClient {
        return with(OkHttpClient.Builder()) {
            withDefaults()
            withProxy(getUserProxyServer())
            addInterceptor(GlideInterceptor(this@ServerManager))
            ignoreSSLCertificateException()
            build()
        }
    }

    private fun changeGlideOkHttpClient() {
        val factory = OkHttpUrlLoader.Factory(getGlideOkHttpClient())
        Glide.get(context).registry.append(GlideUrl::class.java, InputStream::class.java, factory)
    }

    companion object {
        lateinit var endPoint: String
    }
}
