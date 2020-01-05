package com.telex.model.source.local

import com.telex.model.source.local.dao.UserDao
import com.telex.model.source.local.entity.User
import javax.inject.Inject

/**
 * @author Sergey Petrov
 */
class UserLocalDataSource @Inject constructor(database: TelegraphDatabase) {

    private var userDao: UserDao = database.userDao()

    fun getFirstUser() = userDao.getFirstUser()

    fun getUserByAccountName(accountName: String) = userDao.getUserByAccountName(accountName)

    fun observeUser(id: String) = userDao.observeUser(id)

    fun observeAllUsers() = userDao.observeAllUsers()

    fun save(user: User) = userDao.save(user)

    fun clear() = userDao.clear()

    fun delete(id: String) = userDao.delete(id)
}
