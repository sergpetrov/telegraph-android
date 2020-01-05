package com.telex.model.source.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * @author Sergey Petrov
 */
@Entity
data class User(
    @PrimaryKey
    var id: String, // accessToken
    var accountName: String,
    var authorName: String,
    var authorUrl: String?,
    var pageCount: Int = 0
)
