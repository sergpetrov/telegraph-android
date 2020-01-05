package com.telex.model.source.remote.interceptor

import com.telex.utils.Constants.ERROR_ACCESS_TOKEN_INVALID
import com.telex.utils.Constants.ERROR_AUTHOR_URL_INVALID
import com.telex.utils.Constants.ERROR_CONTENT_TEXT_REQUIRED
import com.telex.utils.Constants.ERROR_PAGE_ACCESS_DENIED
import com.telex.utils.Constants.ERROR_PAGE_SAVE_FAILED
import com.telex.utils.Constants.ERROR_SHORT_NAME_REQUIRED
import okhttp3.Interceptor
import okhttp3.Response
import okhttp3.ResponseBody
import org.json.JSONException
import org.json.JSONObject
import timber.log.Timber

/**
 * @author Sergey Petrov
 */
class ErrorsInterceptor : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val response = chain.proceed(chain.request())
        val contentType = response.body()?.contentType()
        val bodyString = response.body()?.string()
        val body = ResponseBody.create(contentType, bodyString)
        try {
            val jsonObject = getBodyAsJsonObject(bodyString)
            if (jsonObject?.has("error") == true) {
                var errorCode = 400
                val error = jsonObject.getString("error")
                when (error) {
                    ERROR_ACCESS_TOKEN_INVALID -> errorCode = 401
                    ERROR_PAGE_ACCESS_DENIED -> errorCode = 403
                    ERROR_AUTHOR_URL_INVALID,
                    ERROR_SHORT_NAME_REQUIRED,
                    ERROR_CONTENT_TEXT_REQUIRED,
                    ERROR_PAGE_SAVE_FAILED -> errorCode = 400
                }
                return response.newBuilder()
                        .code(errorCode)
                        .message(error)
                        .body(retrofit2.Response.error<Any>(errorCode, body).errorBody())
                        .build()
            }
        } catch (error: JSONException) {
            Timber.e(error)
        }
        return response.newBuilder().body(body).build()
    }

    private fun getBodyAsJsonObject(json: String?): JSONObject? {
        var jsonObject: JSONObject? = null
        try {
            jsonObject = JSONObject(json)
        } catch (ignore: JSONException) {
        }
        return jsonObject
    }
}
