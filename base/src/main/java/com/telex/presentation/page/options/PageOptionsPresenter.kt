package com.telex.presentation.page.options

import com.telex.model.interactors.PageInteractor
import com.telex.presentation.base.BasePresenter
import com.telex.presentation.base.ErrorHandler
import javax.inject.Inject
import moxy.InjectViewState

/**
 * @author Sergey Petrov
 */
@InjectViewState
class PageOptionsPresenter @Inject constructor(
    private val pageInteractor: PageInteractor,
    errorHandler: ErrorHandler
) : BasePresenter<PageOptionsView>(errorHandler) {

    fun discardDraft(pageId: Long) {
        pageInteractor.discardDraftPage(pageId)
                .justSubscribe()
    }

    fun deletePost(pageId: Long) {
        pageInteractor.deletePage(pageId)
                .justSubscribe()
    }
}
