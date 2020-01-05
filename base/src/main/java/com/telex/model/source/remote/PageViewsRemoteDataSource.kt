package com.telex.model.source.remote

import com.telex.model.source.remote.api.RestApiProvider
import com.telex.model.source.remote.data.PageViewsData
import io.reactivex.Single
import javax.inject.Inject

/**
 * @author Sergey Petrov
 */
class PageViewsRemoteDataSource @Inject constructor(
    private val apiProvider: RestApiProvider
) {

    fun getPageViews(path: String, year: Int, month: Int?, day: Int?): Single<PageViewsData> {
        return apiProvider.getRestApi()
                .getPageViews(path, year, month, day)
                .map { response -> response.result }
    }
}
