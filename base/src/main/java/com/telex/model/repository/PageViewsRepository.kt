package com.telex.model.repository

import com.telex.model.source.remote.PageViewsRemoteDataSource
import io.reactivex.Single
import javax.inject.Inject

/**
 * @author Sergey Petrov
 */
class PageViewsRepository @Inject constructor(
    private val pageViewsRemoteDataSource: PageViewsRemoteDataSource
) {

    fun getPageViewsForYear(path: String, year: Int): Single<Int> {
        return pageViewsRemoteDataSource.getPageViews(path, year, month = null, day = null)
                .map { result -> result.views }
    }

    fun getPageViewsForMonth(path: String, year: Int, month: Int): Single<Int> {
        return pageViewsRemoteDataSource.getPageViews(path, year, month, day = null)
                .map { result -> result.views }
    }

    fun getPageViewsForDay(path: String, year: Int, month: Int, day: Int): Single<Int> {
        return pageViewsRemoteDataSource.getPageViews(path, year, month, day)
                .map { result -> result.views }
    }
}
