package com.telex.model.source.remote.data

import com.google.gson.annotations.SerializedName

/**
 * @author Sergey Petrov
 */
data class PageData(
    @Transient
    var number: Int?,

    val path: String,
    val url: String,
    val title: String,
    val description: String,
    @SerializedName("author_name")
    val authorName: String,
    @SerializedName("author_url")
    val authorUrl: String,
    @SerializedName("image_url")
    val imageUrl: String?,
    val content: List<NodeElementData>?,
    val views: Int = 0,
    @SerializedName("can_edit")
    val canEdit: Boolean
)
