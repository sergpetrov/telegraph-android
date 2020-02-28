package com.telex.base.presentation.page.options

import com.telex.base.model.interactors.PageInteractor
import com.telex.base.presentation.base.BasePresenter
import com.telex.base.presentation.base.ErrorHandler
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
