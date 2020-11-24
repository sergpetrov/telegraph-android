package com.telex.base.presentation.splash

import android.net.Uri
import com.telex.base.extention.withDefaults
import com.telex.base.model.interactors.PageInteractor
import com.telex.base.model.interactors.UserInteractor
import com.telex.base.model.system.ServerManager
import com.telex.base.presentation.base.BasePresenter
import com.telex.base.presentation.base.ErrorHandler
import io.reactivex.Completable
import moxy.InjectViewState
import timber.log.Timber
import javax.inject.Inject

/**
 * @author Sergey Petrov
 */
@InjectViewState
class SplashPresenter @Inject constructor(
        private val userInteractor: UserInteractor,
        private val pageInteractor: PageInteractor,
        private val serverManager: ServerManager,
        errorHandler: ErrorHandler
) : BasePresenter<SplashView>(errorHandler) {

    fun launch(uri: Uri?) {
        val oauthUrl = uri.toString()

        when {
            uri != null && oauthUrl.isNotEmpty() -> {
                serverManager.checkAvailableServer()
                        .retry(1)
                        .andThen(userInteractor.login(oauthUrl))
                        .onErrorResumeNext { error ->
                            Timber.e(error, "Error during login oauthUrl=$oauthUrl")
                            refreshCurrentAccountAndPages()
                        }
                        .andThen(refreshCurrentAccountAndPages())
                        .doOnSubscribe { viewState.showProgress(true) }
                        .doAfterTerminate {
                            viewState.showProgress(false)
                            viewState.showNext()
                        }
                        .withDefaults()
                        .compositeSubscribe()
            }

            userInteractor.isTokenValid() -> {
                viewState.showNext()

                serverManager.checkAvailableServer()
                        .retry(1)
                        .andThen(refreshCurrentAccountAndPages())
                        .withDefaults()
                        .justSubscribe()
            }

            else -> viewState.onLogout()
        }
    }

    private fun refreshCurrentAccountAndPages(): Completable {
        return Completable.mergeArray(
                userInteractor.refreshCurrentAccount().ignoreElement(),
                pageInteractor.loadPages(offset = 0)
        )
    }
}
