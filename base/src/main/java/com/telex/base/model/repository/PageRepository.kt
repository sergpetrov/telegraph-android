package com.telex.base.model.repository

import com.telex.base.analytics.AnalyticsHelper
import com.telex.base.model.source.local.PageLocalDataSource
import com.telex.base.model.source.local.entity.Nodes
import com.telex.base.model.source.local.entity.Page
import com.telex.base.model.source.local.entity.populate
import com.telex.base.model.source.remote.PageRemoteDataSource
import com.telex.base.model.source.remote.data.NodeElementData
import id.zelory.compressor.Compressor
import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.Observable
import io.reactivex.Single
import java.io.File
import java.util.*
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
                                        resultPages[remotePage.path] = localPage.populate(remotePage)
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
                        .map { page.populate(it.result) }
                        .doOnSuccess { pageLocalDataSource.update(it) }
            }
            else -> Single.just(page)
        }
    }

    fun editPage(pageId: Long, path: String, title: String, authorName: String?, authorUrl: String?, content: List<NodeElementData>, pageImageUrl: String?): Single<Page> {
        return pageRemoteDataSource.editPage(path, title, authorName, authorUrl, content)
                .map { it.result }
                .flatMap { result ->
                    getCachedPage(pageId)
                            .map { localPage ->
                                localPage.populate(result)
                                localPage.draft = false
                                localPage.imageUrl = pageImageUrl
                                localPage
                            }
                }
                .doOnSuccess { page ->
                    AnalyticsHelper.logEditPage()
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
                                            localPage.draft = false
                                            localPage.imageUrl = pageImageUrl
                                            localPage.populate(response.result)
                                        }
                            }
                }
                .doOnSuccess { page ->
                    AnalyticsHelper.logCreatePage()
                    pageLocalDataSource.update(page)
                }
    }

    fun uploadImage(file: File): Observable<String> {
        return imageCompressor.compressToFileAsFlowable(file, file.name + "_compressed").toObservable()
                .flatMap {
                    pageRemoteDataSource.uploadImage(it)
                }
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
