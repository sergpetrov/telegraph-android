package com.telex.base.model.source.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy.REPLACE
import androidx.room.Query
import androidx.room.Update
import com.telex.base.model.source.local.entity.Page
import io.reactivex.Flowable
import io.reactivex.Single

/**
 * @author Sergey Petrov
 */
@Dao
interface PageDao {

    @Query("SELECT * FROM Page WHERE path=:path LIMIT 1")
    fun getPage(path: String): Single<Page>

    @Query("SELECT * FROM Page WHERE id=:id LIMIT 1")
    fun getPage(id: Long): Single<Page>

    @Query("SELECT * FROM Page WHERE userId=:userId")
    fun observePages(userId: String): Flowable<List<Page>>

    @Query("SELECT * FROM Page WHERE draft=1")
    fun observeDraftPages(): Flowable<List<Page>>

    @Query("SELECT * FROM Page WHERE userId=:userId OR draft=1")
    fun getPages(userId: String): Single<List<Page>>

    @Query("SELECT COUNT(draft) FROM Page WHERE draft=1")
    fun observeNumberOfDrafts(): Flowable<Int>

    @Update
    fun update(page: Page)

    @Insert
    fun insert(page: Page): Long

    @Insert(onConflict = REPLACE)
    fun insert(pages: List<Page>)

    @Update
    fun update(pages: List<Page>)

    @Query("DELETE FROM Page WHERE draft=0 AND userId=:userId")
    fun clearExceptDrafts(userId: String)

    @Query("DELETE FROM Page")
    fun clear()

    @Query("DELETE FROM Page WHERE id=:id")
    fun delete(id: Long)
}
