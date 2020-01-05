package com.telex.utils

import android.app.Activity
import android.content.pm.PackageManager.PERMISSION_GRANTED
import android.os.Build
import androidx.core.content.ContextCompat.checkSelfPermission

/**
 * @author Sergey Petrov
 */
object PermissionsHelper {

    fun checkPermissions(activity: Activity, requestCode: Int, vararg permissions: String): Boolean {
        var isGranted = true
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val permissionsList = ArrayList<String>()
            for (p in permissions) {
                if (checkSelfPermission(activity, p) != PERMISSION_GRANTED) {
                    permissionsList.add(p)
                }
            }
            if (!permissionsList.isEmpty()) {
                isGranted = false
                activity.requestPermissions(permissionsList.toTypedArray(), requestCode)
            }
        }
        return isGranted
    }
}
