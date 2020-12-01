package com.telex.base.model.source.local

import android.content.Context
import com.telex.base.utils.Constants.SHARED_DATA
import com.telex.base.utils.ServerConfig
import javax.inject.Inject
import javax.inject.Singleton

/**
 * @author Sergey Petrov
 */
@Singleton
class AppData @Inject constructor(context: Context) {

    private val preferences = context.getSharedPreferences(SHARED_DATA, Context.MODE_PRIVATE)

    fun getCurrentAccessToken(): String? = preferences.getString(ACCESS_TOKEN, null)
    fun putCurrentAccessToken(token: String?) = preferences.edit().putString(ACCESS_TOKEN, token).apply()

    fun getServer(): String = preferences.getString(SERVER, null) ?: ServerConfig.Telegraph.server
    fun putServer(server: String) = preferences.edit().putString(SERVER, server).apply()

    fun needShowAddAccountDialog(): Boolean = preferences.getBoolean(NEED_SHOW_ADD_ACCOUNT_DIALOG, true)
    fun putNeedShowAddAccountDialog(need: Boolean) = preferences.edit().putBoolean(NEED_SHOW_ADD_ACCOUNT_DIALOG, need).apply()

    fun needShowMultipleImagesUploadingDialog(): Boolean = preferences.getBoolean(MULTIPLE_IMAGES_UPLOADING_DIALOG, true)
    fun putNeedShowMultipleImagesUploadingDialog(need: Boolean) = preferences.edit().putBoolean(MULTIPLE_IMAGES_UPLOADING_DIALOG, need).apply()

    fun getLastAppReviewRequestTime(): Long = preferences.getLong(LAST_APP_REVIEW_REQUEST_TIME, -1)
    fun putLastAppReviewRequestTime(time: Long) = preferences.edit().putLong(LAST_APP_REVIEW_REQUEST_TIME, time).apply()

    fun getAuthorizedAppLaunch(): Int = preferences.getInt(AUTHORIZED_APP_LAUNCH, 0)
    fun putAuthorizedAppLaunch(count: Int) = preferences.edit().putInt(AUTHORIZED_APP_LAUNCH, count).apply()

    fun clearAuthData() {
        putCurrentAccessToken(null)
    }

    fun isNightModeEnabled(): Boolean {
        return preferences.getBoolean(NIGHT_MODE_ENABLED, true)
    }

    fun enableNightMode(enable: Boolean) {
        preferences.edit().putBoolean(NIGHT_MODE_ENABLED, enable).apply()
    }

    fun getUserProxyServer(): String? {
        return preferences.getString(USER_PROXY_SERVER, null)
    }

    fun putUserProxyServer(json: String?) {
        preferences.edit().putString(USER_PROXY_SERVER, json).apply()
    }

    companion object {
        private const val SERVER = "SERVER"
        private const val ACCESS_TOKEN = "ACCESS_TOKEN"
        private const val USER_PROXY_SERVER = "USER_PROXY_SERVER"
        private const val NIGHT_MODE_ENABLED = "NIGHT_MODE_ENABLED"
        private const val NEED_SHOW_ADD_ACCOUNT_DIALOG = "NEED_SHOW_ADD_ACCOUNT_DIALOG"
        private const val MULTIPLE_IMAGES_UPLOADING_DIALOG = "MULTIPLE_IMAGES_UPLOADING_DIALOG"
        private const val LAST_APP_REVIEW_REQUEST_TIME = "LAST_APP_REVIEW_REQUEST_TIME"
        private const val AUTHORIZED_APP_LAUNCH = "AUTHORIZED_APP_LAUNCH"
    }
}
