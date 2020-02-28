package com.telex.base.presentation.page.adapter

import com.telex.base.model.interactors.PageInteractor
import com.telex.base.presentation.base.BasePresenter
import com.telex.base.presentation.base.ErrorHandler
import com.telex.base.presentation.page.format.ImageFormat
import java.io.File
import java.net.URI
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import moxy.InjectViewState

/**
 * @author Sergey Petrov
 */
@InjectViewState
class FormatImageItemPresenter @Inject constructor(
    private val pageInteractor: PageInteractor,
    errorHandler: ErrorHandler
) : BasePresenter<FormatImageItemMvpView>(errorHandler) {

    fun uploadImage(format: ImageFormat) {
        pageInteractor.uploadImage(File(URI(format.url)))
                .doOnSubscribe { format.uploadStatus = ImageUploadStatus.InProgress }
                .doOnNext { url ->
                    // update with server url
                    format.url = url
                    format.uploadStatus = ImageUploadStatus.Completed
                    viewState.updateImage(format)
                }
                .doOnError { format.uploadStatus = ImageUploadStatus.Failed }
                .retryWhen { it.delay(3, TimeUnit.SECONDS) }
                .compositeSubscribe()
    }
}
