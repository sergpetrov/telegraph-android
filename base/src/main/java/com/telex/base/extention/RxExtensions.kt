package com.telex.base.extention

import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.Maybe
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import timber.log.Timber

/**
 * @author Sergey Petrov
 */
fun <T> Single<T>.withDefaults(): Single<T> {
    return compose {
        it.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnError { error -> Timber.e(error) }
    }
}

fun <T> Flowable<T>.withDefaults(delayError: Boolean = false): Flowable<T> {
    return compose {
        it.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread(), delayError)
                .doOnError { error -> Timber.e(error) }
    }
}

fun <T> Maybe<T>.withDefaults(): Maybe<T> {
    return compose {
        it.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnError { error -> Timber.e(error) }
    }
}

fun <T> Observable<T>.withDefaults(delayError: Boolean = false): Observable<T> {
    return compose {
        it.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread(), delayError)
                .doOnError { error -> Timber.e(error) }
    }
}

fun Completable.withDefaults(): Completable {
    return compose {
        it.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnError { error -> Timber.e(error) }
    }
}

fun Completable.justSubscribe(
        onComplete: () -> Unit = {},
        onError: (Throwable) -> Unit = { error -> Timber.e(error) }
): Disposable = subscribe({ onComplete.invoke() }, { error -> onError.invoke(error) })

fun <T> Observable<T>.justSubscribe(
        onNext: (T) -> Unit = {},
        onError: (Throwable) -> Unit = { error -> Timber.e(error) }
): Disposable = subscribe({ result -> onNext.invoke(result) }, { error -> onError.invoke(error) })

fun <T> Single<T>.justSubscribe(
        onSuccess: (T) -> Unit = {},
        onError: (Throwable) -> Unit = { error -> Timber.e(error) }
): Disposable = subscribe({ result -> onSuccess.invoke(result) }, { error -> onError.invoke(error) })

fun <T> Maybe<T>.justSubscribe(
        onSuccess: (T) -> Unit = {},
        onError: (Throwable) -> Unit = { error -> Timber.e(error) }
): Disposable = subscribe({ result -> onSuccess.invoke(result) }, { error -> onError.invoke(error) })

