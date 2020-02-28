package com.telex.base.model.interactors

import android.content.res.Resources
import com.telex.base.R
import com.telex.base.extention.withDefaults
import com.telex.base.model.repository.PageRepository
import com.telex.base.model.repository.UserRepository
import com.telex.base.model.source.local.PagedData
import com.telex.base.model.source.local.entity.Page
import com.telex.base.model.source.local.entity.Page.Companion.DELETED_TITLE
import com.telex.base.model.source.remote.data.NodeElementData
import com.telex.base.presentation.page.format.FormatType
import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import java.io.File
import javax.inject.Inject

/**
 * @author Sergey Petrov
 */
class PageInteractor @Inject constructor(
    private val remoteConfigInteractor: RemoteConfigInteractor,
    private val resources: Resources,
    private val pageRepository: PageRepository,
    private val userRepository: UserRepository
) {

    fun publishPage(pageId: Long, pagePath: String?, title: String, authorName: String?, authorUrl: String?, content: List<NodeElementData>, pageImageUrl: String?): Completable {
        return when {
            pagePath != null -> pageRepository.editPage(pageId, pagePath, title, authorName, authorUrl, content, pageImageUrl)
            else -> {

                var resultContent = content
                if (!remoteConfigInteractor.createdWithCaptionDisabled()) {
                    resultContent = addCreatedWithCaption(content)
                }

                pageRepository.createPage(pageId, title, authorName, authorUrl, resultContent, pageImageUrl)
                        .flatMap {
                            userRepository.observeCurrentAccount()
                                    .doOnNext { user ->
                                        user.pageCount += 1
                                        userRepository.saveUser(user)
                                    }.firstOrError()
                        }
            }
        }
                .ignoreElement()
                .withDefaults()
    }

    fun savePageDraft(pageId: Long, title: String, authorName: String?, authorUrl: String?, content: List<NodeElementData>, pageImageUrl: String?): Single<Page> {
        return pageRepository.savePageDraft(pageId, title, authorName, authorUrl, content, pageImageUrl)
                .withDefaults()
    }

    fun getPageOrCreateDraft(pageId: Long?): Single<Page> {
        return when {
            pageId != null -> pageRepository.getAndUpdateCachedPage(pageId)
            else -> pageRepository.createPageDraft(pageId)
        }
    }

    fun discardDraftPage(pageId: Long): Completable {
        return getCachedPage(pageId)
                .flatMapCompletable { page ->
                    discardDraftPage(page)
                }
                .withDefaults()
    }

    fun discardDraftPage(page: Page): Completable {
        return if (page.path.isNullOrEmpty()) {
            pageRepository.deletePage(page.id)
        } else {
            page.draft = false
            pageRepository
                    .updatePage(page)
                    .toSingleDefault(page)
                    .flatMap { pageRepository.getAndUpdateCachedPage(page) }
                    .ignoreElement()
        }.withDefaults()
    }

    fun deletePage(pageId: Long): Completable {
        return getCachedPage(pageId)
                .flatMap { page ->
                    page.title = DELETED_TITLE
                    page.deleted = true

                    pageRepository.editPage(
                            pageId = page.id,
                            path = requireNotNull(page.path),
                            title = requireNotNull(page.title),
                            authorName = DELETED_TITLE,
                            authorUrl = null,
                            content = arrayListOf(NodeElementData(text = " ")),
                            pageImageUrl = null
                    )
                }
                .ignoreElement()
                .withDefaults()
    }

    fun getCachedPage(pagePath: String): Single<Page> {
        return pageRepository.getCachedPage(pagePath)
                .withDefaults()
    }

    fun getCachedPage(pageId: Long?): Single<Page> {
        if (pageId != null) {
            return pageRepository.getCachedPage(pageId)
                    .subscribeOn(Schedulers.io())
        } else {
            return Single.never()
        }
    }

    fun uploadImage(file: File): Observable<String> {
        return pageRepository.uploadImage(file)
                .withDefaults()
    }

    fun observeNumberOfDrafts(): Flowable<Int> {
        return pageRepository.observeNumberOfDrafts()
                .withDefaults()
    }

    fun loadPages(offset: Int = 0, clear: Boolean = false): Completable {
        return pageRepository.loadPages(offset, clear)
                .withDefaults()
    }

    fun observePages(): Flowable<PagedData<Page>> {
        return pageRepository.observePages(userRepository.currentAccountId)
                .flatMapSingle { pages ->
                    val userId = userRepository.currentAccountIdNullable
                    if (userId != null) {
                        userRepository.getUserById(userId)
                                .map { user ->
                                    PagedData(total = user.pageCount, items = pages.sortedByDescending { it.number })
                                }
                    } else Single.just(PagedData(total = 0, items = emptyList()))
                }
                .withDefaults()
    }

    fun observeDraftPages(): Flowable<List<Page>> {
        return pageRepository.observeDraftPages()
                .map { pages -> pages.filter { !it.deleted } }
                .map { pages -> pages.sortedBy { it.number } }
                .withDefaults()
    }

    private fun addCreatedWithCaption(content: List<NodeElementData>): ArrayList<NodeElementData> {
        val result = ArrayList(content)
        result.add(NodeElementData(tag = FormatType.BREAK_LINE.tag))
        result.add(NodeElementData(
                tag = FormatType.PARAGRAPH.tag,
                children = arrayListOf(
                        NodeElementData(text = resources.getString(R.string.created_with)),
                        NodeElementData(
                                tag = FormatType.LINK.tag,
                                attrs = mutableMapOf("href" to resources.getString(R.string.app_link), "_target" to "blank"),
                                children = arrayListOf(NodeElementData(text = resources.getString(R.string.app_name)))
                        )
                ))
        )

        return result
    }
}
