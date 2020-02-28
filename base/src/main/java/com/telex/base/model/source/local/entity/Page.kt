package com.telex.base.model.source.local.entity

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import com.telex.base.model.source.remote.data.PageData

/**
 * @author Sergey Petrov
 */
@Entity
data class Page(
    @PrimaryKey(autoGenerate = true)
    var id: Long,
    var userId: String,
    var number: Int?,
    var path: String?,
    var url: String?,
    var title: String?,
    var imageUrl: String?,
    var views: Int,
    var authorName: String?,
    var authorUrl: String?,
    @Embedded
    var nodes: Nodes,
    var visible: Boolean?,
    var draft: Boolean,
    var deleted: Boolean
) {

    @Ignore
    constructor(userId: String) : this(0, userId, null, null, null, "", null, 0, null, null, Nodes(arrayListOf()), null, false, false)

    fun isEmpty(): Boolean {
        return title.isNullOrBlank() && imageUrl.isNullOrBlank() && nodes.content.isEmpty()
    }

    companion object {

        const val OLD_DELETED_TITLE = "DЕLЕТЕD"
        const val DELETED_TITLE = "DELETED"

        fun convert(page: Page, data: PageData): Page {
            page.path = data.path
            data.number?.let { page.number = data.number }
            page.url = data.url
            page.title = data.title
            page.imageUrl = data.imageUrl
            if (data.views > 0) { // it's api.telegra.ph bug
                page.views = data.views
            }
            page.authorName = data.authorName
            page.authorUrl = data.authorUrl
            data.content?.let { page.nodes = Nodes(it) }

            return page
        }
    }
}
