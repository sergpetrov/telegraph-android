package com.telex.base.model.interactors

import com.telex.base.extention.withDefaults
import com.telex.base.model.repository.PageRepository
import com.telex.base.model.repository.UserRepository
import com.telex.base.model.source.local.AppData
import com.telex.base.model.source.local.entity.User
import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.Observable
import io.reactivex.Single
import javax.inject.Inject

/**
 * @author Sergey Petrov
 */
class UserInteractor @Inject constructor(
    private val userRepository: UserRepository,
    private val pageRepository: PageRepository,
    private val appData: AppData
) {

    val currentAccountId: String?
        get() = userRepository.currentAccountIdNullable

    fun login(oauthUrl: String): Completable {
        return userRepository.login(oauthUrl)
                .withDefaults()
    }

    fun logout(): Completable {
        return Single.fromCallable {
            val userId = currentAccountId

            if (userId != null) {
                userRepository.deleteUser(userId)
                appData.clearAuthData()
                pageRepository.clearExceptDrafts(userId)
            }
        }.flatMapCompletable {
            userRepository.getFirstUser()
                    .doOnSuccess { user ->
                        userRepository.changeCurrentAccount(user)
                    }
                    .ignoreElement()
        }.withDefaults()
    }

    fun refreshCurrentAccount(): Single<User> {
        return userRepository.refreshCurrentAccount()
                .withDefaults()
    }

    fun isTokenValid() = appData.getCurrentAccessToken() != null

    fun observeChangeCurrentAccount(): Observable<User> {
        return userRepository.observeChangeCurrentAccount()
    }

    fun observeCurrentAccount(): Observable<User> {
        return userRepository.observeCurrentAccount()
                .withDefaults()
    }

    fun saveUser(shortName: String, authorName: String?, authorUrl: String?): Single<User> {
        return userRepository.saveUser(shortName, authorName, authorUrl)
                .withDefaults()
    }

    fun observeAllAccounts(): Flowable<List<User>> {
        return userRepository.observeAllAccounts()
                .map { users -> users.sortedBy { it.accountName } }
                .withDefaults()
    }

    fun changeCurrentAccount(user: User) {
        userRepository.changeCurrentAccount(user)
    }
}
