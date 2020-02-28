package com.telex.model.repository

import com.telex.analytics.AnalyticsHelper
import com.telex.model.source.local.PageLocalDataSource
import com.telex.model.source.local.entity.Nodes
import com.telex.model.source.local.entity.Page
import com.telex.model.source.local.entity.Page.Companion.DELETED_TITLE
import com.telex.model.source.local.entity.Page.Companion.OLD_DELETED_TITLE
import com.telex.model.source.remote.PageRemoteDataSource
import com.telex.model.source.remote.data.NodeElementData
import com.telex.model.source.remote.data.PageData
import id.zelory.compressor.Compressor
import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.Observable
import io.reactivex.Single
import java.io.File
import java.util.UUID
import javax.inject.Inject
import kotlin.collections.LinkedHashMap

/**
 * @author Sergey Petrov
 */
class PageRepository @Inject constructor(
    private val pageLocalDataSource: PageLocalDataSource,
    private val pageRemoteDataSource: PageRemoteDataSource,
    private val userRepository: UserRepository,
    private val imageCompressor: Compressor
) {

    private val currentAccountId: String
        get() = userRepository.currentAccountId

    fun loadPages(offset: Int, clear: Boolean): Completable =
            pageRemoteDataSource.getPages(offset)
                    .doOnSuccess {
                        if (clear) {
                            pageLocalDataSource.clearExceptDrafts(currentAccountId)
                        }
                    }
                    .flatMapCompletable { result ->
                        pageLocalDataSource.getPages(currentAccountId)
                                .map { pages ->
                                    pages.map { page ->
                                        val path = page.path ?: "$NEW_PAGE_DRAFT_KEY${UUID.randomUUID()}"
                                        path to page
                                    }.toMap()
                                }
                                .doOnSuccess { localPages ->
                                    val resultPages = LinkedHashMap<String, Page>()

                                    // add a drafts for new pages
                                    var newPageDraftNumber = result.totalCount
                                    for (entry in localPages.entries) {
                                        if (entry.key.startsWith(NEW_PAGE_DRAFT_KEY)) {
                                            val page = entry.value
                                            page.number = newPageDraftNumber
                                            resultPages[entry.key] = page

                                            newPageDraftNumber--
                                        }
                                    }

                                    for (remotePage in result.pages) {
                                        val localPage = localPages[remotePage.path] ?: Page(currentAccountId)
                                        resultPages[remotePage.path] = convertPage(localPage, remotePage)
                                    }

                                    pageLocalDataSource.insert(resultPages.values.toList())
                                }
                                .ignoreElement()
                    }

    fun observePages(userId: String): Flowable<List<Page>> {
        return pageLocalDataSource.observePages(userId)
    }

    fun observeDraftPages(): Flowable<List<Page>> {
        return pageLocalDataSource.observeDraftPages()
    }

    fun observeNumberOfDrafts() = pageLocalDataSource.observeNumberOfDrafts()

    fun getCachedPage(path: String) = pageLocalDataSource.getPage(path)

    fun getCachedPage(id: Long) = pageLocalDataSource.getPage(id)

    fun getAndUpdateCachedPage(id: Long): Single<Page> {
        return getCachedPage(id)
                .flatMap { page -> getAndUpdateCachedPage(page) }
    }

    fun getAndUpdateCachedPage(page: Page): Single<Page> {
        val path = page.path
        return when {
            !page.draft && path != null -> {
                pageRemoteDataSource.getPage(path)
                        .map { convertPage(page, it.result) }
                        .doOnSuccess { pageLocalDataSource.update(it) }
            }
            else -> Single.just(page)
        }
    }

    fun editPage(pageId: Long, path: String, title: String, authorName: String?, authorUrl: String?, content: List<NodeElementData>, pageImageUrl: String?): Single<Page> {
        return pageRemoteDataSource.editPage(path, title, authorName, authorUrl, content)
                .flatMap { convertLocalPage(it.result) }
                .doOnSuccess { page ->
                    AnalyticsHelper.logEditPage()

                    page.id = pageId
                    page.draft = false
                    page.imageUrl = pageImageUrl
                    pageLocalDataSource.update(page)
                }
    }

    fun createPage(pageId: Long, title: String, authorName: String?, authorUrl: String?, content: List<NodeElementData>, pageImageUrl: String?): Single<Page> {
        return pageRemoteDataSource.createPage(title, authorName, authorUrl, content)
                .flatMap { response ->
                    userRepository.getUserById(currentAccountId)
                            .flatMap { user ->
                                getCachedPage(pageId)
                                        .map { localPage ->
                                            localPage.number = user.pageCount // add last number for display on top of list
                                            convertPage(localPage, response.result)
                                        }
                            }
                }
                .doOnSuccess { page ->
                    AnalyticsHelper.logCreatePage()

                    page.draft = false
                    page.imageUrl = pageImageUrl
                    pageLocalDataSource.update(page)
                }
    }

    fun uploadImage(file: File): Observable<String> {
        return imageCompressor.compressToFileAsFlowable(file, file.name + "_compressed").toObservable()
                .flatMap {
                    pageRemoteDataSource.uploadImage(it)
                }
    }

    private fun convertLocalPage(data: PageData): Single<Page> {
        return getCachedPage(data.path)
                .map { convertPage(it, data) }
    }

    private fun convertPage(page: Page, data: PageData): Page {
        val result = Page.convert(page, data)
        result.deleted = data.title == DELETED_TITLE && data.authorName == DELETED_TITLE && data.description.isEmpty() || data.title == OLD_DELETED_TITLE
        return result
    }

    fun savePageDraft(pageId: Long, title: String?, authorName: String?, authorUrl: String?, content: List<NodeElementData>, pageImageUrl: String?): Single<Page> {
        return getCachedPage(pageId)
                .map { page ->
                    page.title = title
                    page.authorName = authorName
                    page.authorUrl = authorUrl
                    page.imageUrl = pageImageUrl
                    page.nodes = Nodes(content)
                    page.draft = true
                    page
                }.doOnSuccess { page ->
                    pageLocalDataSource.update(page)
                }
    }

    fun savePageDraft(page: Page): Completable {
        return Completable.fromCallable {
            page.draft = true
            pageLocalDataSource.update(page)
        }
    }

    fun deletePage(pageId: Long): Completable {
        return Completable.fromCallable { pageLocalDataSource.delete(pageId) }
    }

    fun updatePage(page: Page): Completable {
        return Completable.fromCallable { pageLocalDataSource.update(page) }
    }

    fun createPageDraft(pageId: Long?): Single<Page> {
        return if (pageId != null) {
            getCachedPage(pageId)
                    .doOnSuccess { page ->
                        page.draft = true
                        pageLocalDataSource.update(page)
                    }
        } else {
            Single.fromCallable {
                val page = Page(currentAccountId)
                page.draft = true
                val id = pageLocalDataSource.insert(page)
                page.id = id
                page
            }
        }
    }

    fun clearExceptDrafts(userId: String) {
        pageLocalDataSource.clearExceptDrafts(userId)
    }

    companion object {
        private const val NEW_PAGE_DRAFT_KEY = "new_page_draft"
    }
}
