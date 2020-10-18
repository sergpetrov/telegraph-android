package com.telex.base.presentation.pages

import com.telex.base.model.interactors.PageInteractor
import com.telex.base.model.interactors.UserInteractor
import com.telex.base.presentation.base.BasePresenter
import com.telex.base.presentation.base.ErrorHandler
import io.reactivex.disposables.Disposable
import moxy.InjectViewState
import javax.inject.Inject

/**
 * @author Sergey Petrov
 */
@InjectViewState
class DraftsPresenter @Inject constructor(
        private val userInteractor: UserInteractor,
        private val pageInteractor: PageInteractor,
        errorHandler: ErrorHandler
) : BasePresenter<DraftsView>(errorHandler) {

    private var observePageDisposable: Disposable? = null

    override fun onFirstViewAttach() {
        super.onFirstViewAttach()

        observePages()

        userInteractor.observeChangeCurrentAccount()
                .compositeSubscribe(
                        onNext = {
                            observePages()
                        }
                )
    }

    private fun observePages() {
        observePageDisposable?.dispose()

        observePageDisposable = pageInteractor.observeDraftPages()
                .compositeSubscribe(
                        onNext = { pages ->
                            viewState.showPages(pages, hasMore = false)

                            if (pages.isEmpty()) {
                                viewState.showEmpty()
                            } else {
                                viewState.hideEmpty()
                            }
                        }
                )
    }
}
