package com.telex.base.model.source.remote.data

import com.google.gson.annotations.SerializedName

/**
 * @author Sergey Petrov
 */
data class UserData(
    @SerializedName("short_name")
    val accountName: String,
    @SerializedName("author_name")
    val authorName: String,
    @SerializedName("author_url")
    val authorUrl: String?,
    @SerializedName("access_token")
    val accessToken: String?,
    @SerializedName("auth_url")
    val authUrl: String?,
    @SerializedName("page_count")
    val pageCount: Int = 0
)
