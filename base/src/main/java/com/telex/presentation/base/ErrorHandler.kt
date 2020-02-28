package com.telex.presentation.base

import android.content.res.Resources
import com.telex.R
import com.telex.exceptions.ProxyConnectionException
import com.telex.exceptions.TelegraphUnavailableException
import com.telex.extention.userMessage
import com.telex.model.interactors.UserInteractor
import com.telex.model.system.ServerManager
import com.telex.utils.Constants.ERROR_CODE_UNAUTHORIZED
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import java.io.IOException
import javax.inject.Inject
import retrofit2.HttpException
import timber.log.Timber

/**
 * @author Sergey Petrov
 */
class ErrorHandler @Inject constructor(
        //  private val router: Router,
    private val resources: Resources,
    private val userInteractor: UserInteractor,
    private val serverManager: ServerManager
) {

    fun proceed(error: Throwable, messageListener: (String) -> Unit /*= { router.showSystemMessage(it) }*/) {
        if (error is HttpException) {
            when (error.code()) {
                ERROR_CODE_UNAUTHORIZED -> onLogout()
                else -> handleError(error, messageListener)
            }
        } else {
            handleError(error, messageListener)
        }
    }

    private fun handleError(error: Throwable, messageListener: (String) -> Unit) {
        var message = error.userMessage(resources)

        if (error !is IOException) {
            Timber.e(error)
        } else if (error is ProxyConnectionException) {
            message = resources.getString(R.string.not_working_proxy_error)
        } else if (error is TelegraphUnavailableException) {
            message = resources.getString(R.string.telegraph_unavailable_error)
        }

        messageListener(message)
    }

    private fun onLogout() {
        userInteractor.logout()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({}, {})
    }
}
