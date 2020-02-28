package com.telex.base.presentation.page.dialogs

import com.telex.base.model.interactors.UserInteractor
import com.telex.base.presentation.base.BasePresenter
import com.telex.base.presentation.base.ErrorHandler
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject
import moxy.InjectViewState

/**
 * @author Sergey Petrov
 */
@InjectViewState
class AuthorDialogPresenter @Inject constructor(
    private val userInteractor: UserInteractor,
    errorHandler: ErrorHandler
) : BasePresenter<AuthorView>(errorHandler) {

    fun loadUser() {
        userInteractor.observeCurrentAccount()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .compositeSubscribe(onNext = { viewState.showAuthor(it.authorName, it.authorUrl) })
    }
}
