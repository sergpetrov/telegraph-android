package com.telex.model.system

import android.content.Context
import com.bumptech.glide.Glide
import com.bumptech.glide.integration.okhttp3.OkHttpUrlLoader
import com.bumptech.glide.load.model.GlideUrl
import com.google.gson.Gson
import com.telex.BuildConfig
import com.telex.extention.withDefaults
import com.telex.extention.withProxy
import com.telex.model.source.local.AppData
import com.telex.model.source.local.ProxyServer
import com.telex.model.source.remote.interceptor.AuthInterceptor
import com.telex.model.source.remote.interceptor.ErrorsInterceptor
import com.telex.model.source.remote.interceptor.GlideInterceptor
import com.telex.utils.Constants
import java.io.InputStream
import javax.inject.Inject
import javax.inject.Singleton
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor

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
        return Constants.ServerConfig.apiEndPoint(appData.getServer())
    }

    fun getEndPoint(): String {
        return Constants.ServerConfig.endPoint(appData.getServer())
    }

    fun getImageUploadEndPoint(): String {
        return Constants.ServerConfig.imageUploadEndPoint(appData.getServer())
    }

    fun getCurrentServer(): String {
        return appData.getServer()
    }

    fun changeServer() {
        appData.putServer(Constants.graphServer)
        endPoint = getEndPoint()
        isServerConfigChanged = true
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

            build()
        }
    }

    fun getGlideOkHttpClient(): OkHttpClient {
        return with(OkHttpClient.Builder()) {
            withDefaults()
            withProxy(getUserProxyServer())
            addInterceptor(GlideInterceptor(this@ServerManager))
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
