package com.telex.base.presentation.settings.account

import com.telex.base.analytics.AnalyticsHelper
import com.telex.base.model.interactors.UserInteractor
import com.telex.base.presentation.base.BasePresenter
import com.telex.base.presentation.base.ErrorHandler
import moxy.InjectViewState
import javax.inject.Inject

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
