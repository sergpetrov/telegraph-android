package com.telex.presentation.base

import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import moxy.MvpPresenter

/**
 * @author Sergey Petrov
 */
abstract class BasePresenter<View : BaseMvpView>(
    protected val errorHandler: ErrorHandler
) : MvpPresenter<View>() {

    protected val compositeDisposable by lazy { CompositeDisposable() }

    protected val defaultOnErrorConsumer: (Throwable) -> Unit by lazy { OnErrorConsumer() }

    protected fun Completable.compositeSubscribe(
        onSuccess: () -> Unit = {},
        onError: (Throwable) -> Unit = defaultOnErrorConsumer
    ) = subscribe(onSuccess, onError)
            .composite()

    protected fun Completable.justSubscribe(
        onSuccess: () -> Unit = {},
        onError: (Throwable) -> Unit = defaultOnErrorConsumer
    ) = subscribe(onSuccess, onError)

    protected fun <T : Any> Flowable<T>.compositeSubscribe(
        onNext: (T) -> Unit = {},
        onError: (Throwable) -> Unit = defaultOnErrorConsumer,
        onComplete: () -> Unit = {}
    ) = subscribe(onNext, onError, onComplete)
            .composite()

    protected fun <T : Any> Single<T>.compositeSubscribe(
        onSuccess: (T) -> Unit = {},
        onError: (Throwable) -> Unit = defaultOnErrorConsumer
    ) = subscribe(onSuccess, onError)
            .composite()

    protected fun <T : Any> Observable<T>.compositeSubscribe(
        onNext: (T) -> Unit = {},
        onError: (Throwable) -> Unit = defaultOnErrorConsumer,
        onComplete: () -> Unit = {}
    ) = subscribe(onNext, onError, onComplete)
            .composite()

    protected fun <T : Any> Observable<T>.justSubscribe(
        onNext: (T) -> Unit = {},
        onError: (Throwable) -> Unit = defaultOnErrorConsumer,
        onComplete: () -> Unit = {}
    ) = subscribe(onNext, onError, onComplete)

    private fun Disposable.composite(): Disposable {
        compositeDisposable.add(this)
        return this
    }

    override fun onDestroy() {
        compositeDisposable.dispose()
        super.onDestroy()
    }

    open inner class OnErrorConsumer : BaseOnErrorConsumer() {

        override fun onError(error: Throwable) {
            errorHandler.proceed(error) { message -> viewState.showError(message) }
        }
    }
}
