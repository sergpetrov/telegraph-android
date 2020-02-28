package com.telex.base.utils

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkInfo

/**
 * @author Sergey Petrov
 */
object NetworkUtils {

    fun hasNetwork(context: Context): Boolean {
        var isConnected = false
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetwork: NetworkInfo? = connectivityManager.activeNetworkInfo
        if (activeNetwork != null && activeNetwork.isConnected) {
            isConnected = true
        }
        return isConnected
    }
}
