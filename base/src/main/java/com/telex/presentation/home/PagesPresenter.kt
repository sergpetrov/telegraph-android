package com.telex.presentation.home

import android.content.Context
import com.telex.extention.withDefaults
import com.telex.model.interactors.PageInteractor
import com.telex.model.interactors.RemoteConfigInteractor
import com.telex.model.interactors.UserInteractor
import com.telex.model.source.local.entity.Page
import com.telex.presentation.Router
import com.telex.presentation.base.BasePresenter
import com.telex.presentation.base.ErrorHandler
import com.telex.presentation.page.EditorMode
import io.reactivex.Single
import io.reactivex.disposables.Disposable
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import moxy.InjectViewState

/**
 * @author Sergey Petrov
 */
@InjectViewState
class PagesPresenter @Inject constructor(
    private val userInteractor: UserInteractor,
    private val pageInteractor: PageInteractor,
    private val router: Router,
    private val remoteConfigInteractor: RemoteConfigInteractor,
    errorHandler: ErrorHandler
) : BasePresenter<PagesView>(errorHandler) {

    var drafts: Boolean = false

    private var offset = 0
    private var pages = emptyList<Page>()
    private var observePageDisposable: Disposable? = null

    override fun onFirstViewAttach() {
        super.onFirstViewAttach()

        observePages(drafts)

        userInteractor.observeChangeCurrentAccount()
                .compositeSubscribe(
                        onNext = {
                            offset = 0
                            pages = emptyList()
                            observePages(drafts)
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
                .flatMapCompletable { pageInteractor.loadPages(offset = offset, clear = true) }
                .doOnSubscribe { viewState.showProgress(true) }
                .doAfterTerminate { viewState.showProgress(false) }
                .compositeSubscribe()
    }

    fun showNewPageActivity(context: Context, page: Page, mode: EditorMode) {
        router.showNewPageActivity(context, page, mode)
    }

    private fun observePages(drafts: Boolean) {
        observePageDisposable?.dispose()

        if (drafts) {
            observeDraftPages()
        } else {
            observePages()
        }
    }

    private fun observeDraftPages() {
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

    private fun observePages() {
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
