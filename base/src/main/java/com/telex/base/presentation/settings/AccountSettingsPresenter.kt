package com.telex.base.presentation.settings

import com.telex.base.analytics.AnalyticsHelper
import com.telex.base.model.interactors.UserInteractor
import com.telex.base.presentation.base.BasePresenter
import com.telex.base.presentation.base.ErrorHandler
import javax.inject.Inject
import moxy.InjectViewState

/**
 * @author Sergey Petrov
 */
@InjectViewState
class AccountSettingsPresenter @Inject constructor(
    private val userInteractor: UserInteractor,
    errorHandler: ErrorHandler
) : BasePresenter<AccountSettingsView>(errorHandler) {

    override fun onFirstViewAttach() {
        super.onFirstViewAttach()
        loadUser()
    }

    fun saveUser(shortName: String, authorName: String?, authorUrl: String?) {
        userInteractor.saveUser(shortName, authorName, authorUrl)
                .doOnSubscribe { viewState.showProgress(true) }
                .doOnError { viewState.showProgress(false) }
                .compositeSubscribe(onSuccess = { viewState.onUserSaved() })
    }

    fun logout() {
        userInteractor.logout()
                .doOnSubscribe { viewState.showProgress(true) }
                .doAfterTerminate { viewState.showProgress(false) }
                .compositeSubscribe(
                        onSuccess = {
                            AnalyticsHelper.logLogout()
                            viewState.onLogout()
                        }
                )
    }

    private fun loadUser() {
        userInteractor.observeCurrentAccount()
                .compositeSubscribe(onNext = { viewState.showUser(it) })
    }
}
