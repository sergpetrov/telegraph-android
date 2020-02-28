package com.telex.base.model.source.remote.data

import com.google.gson.annotations.SerializedName

/**
 * @author Sergey Petrov
 */
data class PageListData(
    @SerializedName("total_count")
    val totalCount: Int,
    val pages: List<PageData>
)
