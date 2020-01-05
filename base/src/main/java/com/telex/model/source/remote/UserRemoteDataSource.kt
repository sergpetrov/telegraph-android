package com.telex.model.source.remote

import com.telex.model.source.remote.api.RestApiProvider
import com.telex.model.source.remote.data.UserData
import io.reactivex.Completable
import io.reactivex.Single
import javax.inject.Inject

/**
 * @author Sergey Petrov
 */
class UserRemoteDataSource @Inject constructor(private val apiProvider: RestApiProvider) {

    fun getAccountInfo(): Single<UserData> = apiProvider.getRestApi().getAccountInfo()
            .map { r -> r.result }

    fun editAccountInfo(shortName: String, authorName: String?, authorUrl: String?) =
            apiProvider.getRestApi().editAccountInfo(shortName, authorName, authorUrl)
            .map { r -> r.result }

    fun login(oauthUrl: String): Completable = apiProvider.getRestApi().login(oauthUrl).ignoreElement()

    fun resetSessions() = apiProvider.getRestApi().revokeAccessToken()
}
