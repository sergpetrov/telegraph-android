package com.telex.base.presentation

import com.telex.base.presentation.base.BasePresenter
import com.telex.base.presentation.base.ErrorHandler
import moxy.InjectViewState
import javax.inject.Inject

/**
 * @author Sergey Petrov
 */
@InjectViewState
class AppActivityPresenter @Inject constructor(
        errorHandler: ErrorHandler
) : BasePresenter<AppActivityView>(errorHandler)