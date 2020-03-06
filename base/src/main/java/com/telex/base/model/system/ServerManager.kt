package com.telex.base.model.system

import android.content.Context
import com.bumptech.glide.Glide
import com.bumptech.glide.integration.okhttp3.OkHttpUrlLoader
import com.bumptech.glide.load.model.GlideUrl
import com.google.gson.Gson
import com.telex.base.BuildConfig
import com.telex.base.exceptions.NoNetworkConnectionException
import com.telex.base.exceptions.ProxyConnectionException
import com.telex.base.exceptions.TelegraphUnavailableException
import com.telex.base.extention.ignoreSSLCertificateException
import com.telex.base.extention.isOnline
import com.telex.base.extention.withDefaults
import com.telex.base.model.source.local.AppData
import com.telex.base.model.source.local.ProxyServer
import com.telex.base.model.source.remote.interceptor.AuthInterceptor
import com.telex.base.model.source.remote.interceptor.ErrorsInterceptor
import com.telex.base.model.source.remote.interceptor.GlideInterceptor
import com.telex.base.utils.ServerConfig
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
        return Completable.create { emitter ->
            if (context.isOnline()) {
                val config = findAvailableServer()
                if (config != null) {
                    appData.putServer(config.server)
                    endPoint = getEndPoint()
                    isServerConfigChanged = true
                    emitter.onComplete()
                } else {
                    val error = when {
                        isUserProxyServerEnabled() -> ProxyConnectionException()
                        else -> TelegraphUnavailableException()
                    }
                    emitter.tryOnError(error)
                }
            } else {
                emitter.tryOnError(NoNetworkConnectionException())
            }
        }
    }

    private fun findAvailableServer(): ServerConfig? {
        val currentServerConfig = getCurrentServerConfig()
        return when {
            isServerAvailable(currentServerConfig) -> currentServerConfig
            else -> {
                ServerConfig.values().forEach { config ->
                    if (config != currentServerConfig && isServerAvailable(config)) {
                        return config
                    }
                }
                null
            }
        }
    }

    private fun isServerAvailable(config: ServerConfig): Boolean {
        var connection: HttpURLConnection? = null
        try {
            val proxy = configureProxy(getUserProxyServer())
            val url = URL(config.endPoint)
            connection = if (proxy != null) {
                url.openConnection(proxy)
            } else {
                url.openConnection()
            } as HttpURLConnection

            connection.connectTimeout = 5000
            connection.connect()
            return true
        } catch (error: IOException) {
            Timber.e(error)
        } finally {
            connection?.disconnect()
        }
        return false
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
            proxy(configureProxy(getUserProxyServer()))

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
            proxy(configureProxy(getUserProxyServer()))
            addInterceptor(GlideInterceptor(this@ServerManager))
            ignoreSSLCertificateException()
            build()
        }
    }

    private fun changeGlideOkHttpClient() {
        val factory = OkHttpUrlLoader.Factory(getGlideOkHttpClient())
        Glide.get(context).registry.append(GlideUrl::class.java, InputStream::class.java, factory)
    }

    private fun configureProxy(userProxyServer: ProxyServer?): Proxy? {
        val proxyServer = if (userProxyServer != null && userProxyServer.enabled) userProxyServer else null
        return if (proxyServer != null) {
            Authenticator.setDefault(object : Authenticator() {
                override fun getPasswordAuthentication(): PasswordAuthentication {
                    return PasswordAuthentication(proxyServer.user, proxyServer.password?.toCharArray())
                }
            })
            Proxy(Proxy.Type.valueOf(proxyServer.type.name), InetSocketAddress.createUnresolved(proxyServer.host, proxyServer.port))
        } else {
            Authenticator.setDefault(null)
            null
        }
    }

    companion object {
        lateinit var endPoint: String
    }
}
