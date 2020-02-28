package com.telex.base.model.source.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.telex.base.model.source.local.entity.User
import io.reactivex.Flowable
import io.reactivex.Maybe

/**
 * @author Sergey Petrov
 */
@Dao
interface UserDao {

    @Query("SELECT * FROM User ORDER BY accountName LIMIT 1")
    fun getFirstUser(): Maybe<User>

    @Query("SELECT * FROM User WHERE accountName=:accountName LIMIT 1")
    fun getUserByAccountName(accountName: String): User?

    @Query("SELECT * FROM User WHERE id=:id LIMIT 1")
    fun observeUser(id: String): Flowable<User>

    @Query("SELECT * FROM User")
    fun observeAllUsers(): Flowable<List<User>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun save(user: User)

    @Query("DELETE FROM User")
    fun clear()

    @Query("DELETE FROM User WHERE id=:id")
    fun delete(id: String)
}
