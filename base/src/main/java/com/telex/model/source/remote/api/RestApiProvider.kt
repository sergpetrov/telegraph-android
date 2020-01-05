package com.telex.model.source.remote.api

import com.google.gson.GsonBuilder
import com.telex.model.source.remote.data.NodeElementData
import com.telex.model.source.remote.mapper.NodeElementJsonDeserializer
import com.telex.model.system.ServerManager
import javax.inject.Inject
import javax.inject.Singleton
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory

/**
 * @author Sergey Petrov
 */
@Singleton
class RestApiProvider @Inject constructor(
    private val serverManager: ServerManager
) {

    private var api: RestApi? = null

    fun getRestApi(): RestApi {
        // TODO replace it to implementation with di scope
        if (api == null || serverManager.isServerConfigChanged) {
            serverManager.isServerConfigChanged = false
            initRestApi()
        }
        return api ?: throw IllegalStateException("api can't be null")
    }

    private fun initRestApi() {
        val gson = GsonBuilder()
                .registerTypeAdapter(NodeElementData::class.java, NodeElementJsonDeserializer()).create()

        val retrofit = Retrofit.Builder()
                .baseUrl(serverManager.getApiEndPoint())
                .addConverterFactory(GsonConverterFactory.create(gson))
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .client(serverManager.getOkHttpClient())
                .build()
        api = retrofit.create(RestApi::class.java)
    }
}
