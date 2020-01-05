package com.telex.presentation.home

import android.net.Uri
import com.telex.model.interactors.PageInteractor
import com.telex.model.interactors.UserInteractor
import com.telex.presentation.base.BasePresenter
import com.telex.presentation.base.ErrorHandler
import io.reactivex.Completable
import javax.inject.Inject
import moxy.InjectViewState

/**
 * @author Sergey Petrov
 */
@InjectViewState
class LaunchPresenter @Inject constructor(
    private val userInteractor: UserInteractor,
    private val pageInteractor: PageInteractor,
    errorHandler: ErrorHandler
) : BasePresenter<LaunchView>(errorHandler) {

    fun login(uri: Uri?) {
        when {
            uri != null -> {
                val oauthUrl = uri.toString()
                if (oauthUrl.isNotEmpty()) {
                    userInteractor.login(oauthUrl)
                            .doOnError { viewState.showNext() }
                            .andThen(
                                    userInteractor.refreshCurrentAccount()
                                            .flatMapCompletable { refreshPages() }
                            )
                            .compositeSubscribe()
                }
            }

            userInteractor.isTokenValid() -> {
                userInteractor.refreshCurrentAccount()
                        .doOnError { viewState.showNext() }
                        .flatMapCompletable { refreshPages() }
                        .compositeSubscribe()
            }

            else -> {
                viewState.onLogout()
            }
        }
    }

    private fun refreshPages(): Completable {
        return pageInteractor.loadPages(offset = 0)
                .doOnSubscribe { viewState.showProgress(true) }
                .doOnError { viewState.showProgress(false) }
                .doOnComplete { viewState.showNext() }
    }
}
