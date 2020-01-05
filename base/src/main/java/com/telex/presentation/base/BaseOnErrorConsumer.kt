package com.telex.presentation.base

/**
 * @author Sergey Petrov
 */
abstract class BaseOnErrorConsumer : (Throwable) -> Unit {

    override fun invoke(error: Throwable) {
        onError(error)
    }

    abstract fun onError(error: Throwable)
}
