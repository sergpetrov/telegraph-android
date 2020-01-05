package com.telex.presentation.page

import com.telex.presentation.page.format.Format

/**
 * @author Sergey Petrov
 */
data class DraftFields(
    val title: String,
    val authorName: String?,
    val authorUrl: String?,
    val formats: ArrayList<Format>
)
