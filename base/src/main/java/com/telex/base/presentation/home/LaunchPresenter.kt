package com.telex.base.presentation.home

import android.net.Uri
import com.telex.base.extention.withDefaults
import com.telex.base.model.interactors.PageInteractor
import com.telex.base.model.interactors.UserInteractor
import com.telex.base.model.system.ServerManager
import com.telex.base.presentation.base.BasePresenter
import com.telex.base.presentation.base.ErrorHandler
import io.reactivex.Completable
import javax.inject.Inject
import moxy.InjectViewState
import timber.log.Timber

/**
 * @author Sergey Petrov
 */
@InjectViewState
class LaunchPresenter @Inject constructor(
    private val userInteractor: UserInteractor,
    private val pageInteractor: PageInteractor,
    private val serverManager: ServerManager,
    errorHandler: ErrorHandler
) : BasePresenter<LaunchView>(errorHandler) {

    fun launch(uri: Uri?) {
        serverManager.checkAvailableServer()
                .withDefaults()
                .andThen(login(uri))
                .doOnError { viewState.showNext() }
                .compositeSubscribe()
    }

    private fun login(uri: Uri?): Completable {
        val oauthUrl = uri.toString()

        return when {
            uri != null && oauthUrl.isNotEmpty() -> {
                userInteractor.login(oauthUrl)
                        .onErrorResumeNext { error ->
                            Timber.e(error, "Error during login oauthUrl=$oauthUrl")
                            refreshCurrentAccountAndPages()
                        }
                        .andThen(refreshCurrentAccountAndPages())
            }

            userInteractor.isTokenValid() -> refreshCurrentAccountAndPages()

            else -> {
                Completable.fromCallable {
                    viewState.onLogout()
                }.withDefaults()
            }
        }
    }

    private fun refreshCurrentAccountAndPages(): Completable {
        return userInteractor.refreshCurrentAccount()
                .flatMapCompletable { pageInteractor.loadPages(offset = 0) }
                .doOnSubscribe { viewState.showProgress(true) }
                .doOnError { viewState.showProgress(false) }
                .doOnComplete { viewState.showNext() }
    }
}
