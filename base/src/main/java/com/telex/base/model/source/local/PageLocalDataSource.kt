package com.telex.base.model.source.local

import com.telex.base.model.source.local.entity.Page
import javax.inject.Inject

/**
 * @author Sergey Petrov
 */
class PageLocalDataSource @Inject constructor(database: TelegraphDatabase) {
    private var pageDao = database.pageDao()

    fun getPages(userId: String) = pageDao.getPages(userId)

    fun observePages(userId: String) = pageDao.observePages(userId)

    fun observeDraftPages() = pageDao.observeDraftPages()

    fun observeNumberOfDrafts() = pageDao.observeNumberOfDrafts()

    fun getPage(path: String) = pageDao.getPage(path)

    fun getPage(id: Long) = pageDao.getPage(id)

    fun insert(pages: List<Page>) = pageDao.insert(pages)

    fun update(pages: List<Page>) = pageDao.update(pages)

    fun update(page: Page) = pageDao.update(page)

    fun insert(page: Page) = pageDao.insert(page)

    fun clearExceptDrafts(userId: String) = pageDao.clearExceptDrafts(userId)

    fun clear() = pageDao.clear()

    fun delete(pageId: Long) {
        pageDao.delete(pageId)
    }
}
