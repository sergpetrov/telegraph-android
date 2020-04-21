package com.telex.base.presentation.pages

import com.telex.base.extention.withDefaults
import com.telex.base.model.interactors.PageInteractor
import com.telex.base.model.interactors.RemoteConfigInteractor
import com.telex.base.model.interactors.UserInteractor
import com.telex.base.model.source.local.entity.Page
import com.telex.base.presentation.base.BasePresenter
import com.telex.base.presentation.base.ErrorHandler
import io.reactivex.Single
import io.reactivex.disposables.Disposable
import moxy.InjectViewState
import java.util.concurrent.TimeUnit
import javax.inject.Inject

/**
 * @author Sergey Petrov
 */
@InjectViewState
class PagesPresenter @Inject constructor(
    private val userInteractor: UserInteractor,
    private val pageInteractor: PageInteractor,
    private val remoteConfigInteractor: RemoteConfigInteractor,
    errorHandler: ErrorHandler
) : BasePresenter<PagesView>(errorHandler) {

    private var offset = 0
    private var pages = emptyList<Page>()
    private var observePageDisposable: Disposable? = null

    override fun onFirstViewAttach() {
        super.onFirstViewAttach()

        observePages()

        userInteractor.observeChangeCurrentAccount()
                .compositeSubscribe(
                        onNext = {
                            offset = 0
                            pages = emptyList()
                            observePages()
                        }
                )
    }

    fun loadMorePages() {
        pageInteractor.loadPages(offset = offset)
                .compositeSubscribe()
    }

    fun refreshUserAndPages() {
        offset = 0
        userInteractor.refreshCurrentAccount()
                .flatMapCompletable { pageInteractor.loadPages(offset = offset) }
                .doOnSubscribe { viewState.showProgress(true) }
                .doAfterTerminate { viewState.showProgress(false) }
                .compositeSubscribe()
    }

    private fun observePages() {
        observePageDisposable?.dispose()
        observePageDisposable = pageInteractor.observePages()
                .doOnNext { pagedData ->
                    val allPagesExceptDrafts = pagedData.items.filterNot { it.draft }
                    pages = allPagesExceptDrafts.filterNot { it.deleted }
                    val hasMore = pagedData.items.size < pagedData.total

                    showData(hasMore)

                    offset = allPagesExceptDrafts.size

                    if (!hasMore && allPagesExceptDrafts.isEmpty()) {
                        viewState.showEmpty()
                    } else {
                        viewState.hideEmpty()
                    }
                }
                .compositeSubscribe()
    }

    private fun showData(hasMore: Boolean) {
        viewState.showPages(pages, hasMore)

        if (hasMore && pages.isNotEmpty()) {
            viewState.showAdapterProgress()
        } else {
            viewState.hideAdapterProgress()
        }

        if (offset == 0) {
            if (!hasMore && pages.isEmpty()) {
                viewState.hideTopBanner()
            } else {
                showTopBanner()
            }
        }
    }

    private fun showTopBanner() {
        Single.timer(1, TimeUnit.SECONDS)
                .withDefaults()
                .compositeSubscribe(
                        onSuccess = {
                            val banner = remoteConfigInteractor.getTopBanner()
                            if (banner != null) {
                                viewState.showTopBanner(banner)
                            }
                        }
                )
    }
}
