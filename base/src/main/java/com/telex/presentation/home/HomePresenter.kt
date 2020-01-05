package com.telex.presentation.home

import com.telex.analytics.AnalyticsHelper
import com.telex.model.interactors.PageInteractor
import com.telex.model.interactors.UserInteractor
import com.telex.model.source.local.AppData
import com.telex.model.source.local.entity.User
import com.telex.model.system.ServerManager
import com.telex.presentation.base.BasePresenter
import com.telex.presentation.base.ErrorHandler
import io.reactivex.disposables.Disposable
import javax.inject.Inject
import moxy.InjectViewState

/**
 * @author Sergey Petrov
 */
@InjectViewState
class HomePresenter @Inject constructor(
    private val appData: AppData,
    private val serverManager: ServerManager,
    private val userInteractor: UserInteractor,
    private val pageInteractor: PageInteractor,
    errorHandler: ErrorHandler
) : BasePresenter<HomeView>(errorHandler) {

    private var observeAllAccountsDisposable: Disposable? = null
    private var observeNumberOfDraftsDisposable: Disposable? = null

    override fun onFirstViewAttach() {
        viewState.updateNightMode(appData.isNightModeEnabled(), needRecreate = false)

        if (!userInteractor.isTokenValid()) {
            viewState.setupUnauthorized()
        } else {
            viewState.setupAuthorized()

            observeAllAccounts()
            observeNumberOfDrafts()
        }

        userInteractor.observeChangeCurrentAccount()
                .compositeSubscribe(
                        onNext = {
                            observeAllAccounts()
                            observeNumberOfDrafts()

                            userInteractor.refreshCurrentAccount()
                                    .flatMapCompletable { pageInteractor.loadPages(offset = 0) }
                                    .justSubscribe()
                        }
                )
    }

    override fun attachView(view: HomeView?) {
        super.attachView(view)

        if (serverManager.isUserProxyServerEnabled()) {
            viewState.showProxyServerEnabled()
        } else {
            viewState.showProxyServerDisabled()
        }
    }

    private fun observeAllAccounts() {
        observeAllAccountsDisposable?.dispose()

        observeAllAccountsDisposable = userInteractor.observeAllAccounts()
                .compositeSubscribe(
                        onNext = { users ->
                            val currentAccount = users.find { it.id == userInteractor.currentAccountId }
                            if (currentAccount != null) {
                                viewState.showCurrentAccount(currentAccount)
                            }
                            viewState.showAccounts(users.filter { it.id != userInteractor.currentAccountId })
                        }
                )
    }

    private fun observeNumberOfDrafts() {
        observeNumberOfDraftsDisposable?.dispose()

        observeNumberOfDraftsDisposable = pageInteractor.observeNumberOfDrafts()
                .compositeSubscribe(
                        onNext = { viewState.showDraftsCount(it) }
                )
    }

    fun changeCurrentAccount(user: User) {
        userInteractor.changeCurrentAccount(user)
    }

    fun enableProxyServer(enable: Boolean) {
        if (serverManager.getUserProxyServer() != null) {
            serverManager.enableUserProxyServer(enable)
        } else {
            viewState.showProxyServerNotExist()
        }

        if (enable) {
            AnalyticsHelper.logProxyOn()
        } else {
            AnalyticsHelper.logProxyOff()
        }
    }

    fun switchNightMode(enable: Boolean) {
        appData.enableNightMode(enable)
        viewState.updateNightMode(nightModeEnabled = enable, needRecreate = true)
    }
}
