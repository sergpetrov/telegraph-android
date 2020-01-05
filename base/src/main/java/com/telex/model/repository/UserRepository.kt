package com.telex.model.repository

import com.telex.analytics.AnalyticsHelper
import com.telex.extention.toNetworkUrl
import com.telex.model.source.local.AppData
import com.telex.model.source.local.UserLocalDataSource
import com.telex.model.source.local.entity.User
import com.telex.model.source.remote.UserRemoteDataSource
import com.telex.model.source.remote.data.UserData
import com.telex.model.system.ServerManager
import com.telex.utils.Constants
import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.Maybe
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.subjects.PublishSubject
import java.io.IOException
import javax.inject.Inject

/**
 * @author Sergey Petrov
 */
class UserRepository @Inject constructor(
    private val userLocalDataSource: UserLocalDataSource,
    private val userRemoteDataSource: UserRemoteDataSource,
    private val serverManager: ServerManager,
    private val appData: AppData
) {

    val currentAccountIdNullable: String?
        get() = appData.getCurrentAccessToken()

    val currentAccountId: String
        get() = appData.getCurrentAccessToken() ?: throw IllegalArgumentException("currentAccountId can't be null")

    fun changeCurrentAccount(user: User) {
        appData.putCurrentAccessToken(user.id)
        changeCurrentAccountObserver.onNext(user)
    }

    fun refreshCurrentAccount(): Single<User> {
        return userRemoteDataSource.getAccountInfo()
                .onErrorResumeNext { error ->
                    if (error is IOException) {
                        serverManager.changeServer()
                        userRemoteDataSource.getAccountInfo()
                    } else Single.error(error)
                }
                .map { userData ->
                    val user = userLocalDataSource.getUserByAccountName(userData.accountName)
                    if (user != null) {
                        userLocalDataSource.delete(user.id)
                    }
                    User(currentAccountId, userData.accountName, userData.authorName, userData.authorUrl, userData.pageCount)
                }
                .doOnSuccess { saveUser(it) }
    }

    fun observeCurrentAccount(): Observable<User> {
        return if (currentAccountIdNullable != null) {
            userLocalDataSource.observeUser(currentAccountId).toObservable()
        } else Observable.empty<User>()
    }

    fun observeChangeCurrentAccount(): Observable<User> {
        return changeCurrentAccountObserver
    }

    fun observeAllAccounts(): Flowable<List<User>> {
        return userLocalDataSource.observeAllUsers()
    }

    fun saveUser(shortName: String, authorName: String?, authorUrl: String?): Single<User> {
        return userRemoteDataSource.editAccountInfo(shortName, authorName, authorUrl?.toNetworkUrl())
                .flatMap { userData ->
                    getUserById(currentAccountId)
                            .map { convertUser(it, userData) }
                }
                .doOnSuccess { user ->
                    AnalyticsHelper.logEditAccountInfo()

                    userLocalDataSource.save(user)
                }
    }

    fun getUserById(id: String): Single<User> {
        return userLocalDataSource.observeUser(id)
                .firstOrError()
    }

    fun getFirstUser(): Maybe<User> {
        return userLocalDataSource.getFirstUser()
    }

    fun login(oauthUrl: String): Completable {
        return userRemoteDataSource.login(changeOAuthUrlIfNeeded(oauthUrl))
                .onErrorResumeNext { error ->
                    if (error is IOException) {
                        serverManager.changeServer()
                        userRemoteDataSource.login(changeOAuthUrlIfNeeded(oauthUrl))
                    } else Completable.error(error)
                }
    }

    private fun changeOAuthUrlIfNeeded(oauthUrl: String): String {
        return if (serverManager.getEndPoint().contains(Constants.graphServer)) {
            oauthUrl.replace(Constants.telegraphServer, Constants.graphServer)
        } else oauthUrl
    }

    private fun convertUser(user: User, data: UserData): User {
        user.accountName = data.accountName
        user.authorName = data.authorName
        user.authorUrl = data.authorUrl
        return user
    }

    fun saveUser(user: User) {
        userLocalDataSource.save(user)
    }

    fun deleteUser(userId: String) {
        userLocalDataSource.delete(userId)
    }

    companion object {
        private val changeCurrentAccountObserver = PublishSubject.create<User>()
    }
}
