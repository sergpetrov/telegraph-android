package com.telex.utils

import android.util.Log
import com.bumptech.glide.load.engine.GlideException
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.telex.base.exceptions.NoNetworkConnectionException
import com.telex.base.exceptions.ProxyConnectionException
import java.io.IOException
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import javax.net.ssl.SSLHandshakeException
import timber.log.Timber

/**
 * @author Sergey Petrov
 */
class DebugTree : Timber.DebugTree()

class ReleaseTree : Timber.Tree() {
    override fun log(priority: Int, tag: String?, message: String, throwable: Throwable?) {
        if (priority == Log.ERROR) {
            if (throwable !is GlideException || throwable.rootCauses.find { it is IOException } == null) {
                val crashlytics = FirebaseCrashlytics.getInstance()
                if (throwable == null) {
                    crashlytics.recordException(UnknownError(message))
                } else if (!IGNORED_ERRORS.contains(throwable::class.java)) {
                    crashlytics.recordException(throwable)
                }
            }
        }
    }

    companion object {
        private val IGNORED_ERRORS =
                arrayListOf(
                        SSLHandshakeException::class.java,
                        ConnectException::class.java,
                        UnknownHostException::class.java,
                        SocketTimeoutException::class.java,
                        ProxyConnectionException::class.java,
                        NoNetworkConnectionException::class.java
                )
    }
}
