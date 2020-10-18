package com.telex.base.presentation.page

import com.telex.base.presentation.page.format.Format

/**
 * @author Sergey Petrov
 */
data class DraftFields(
    val title: String,
    val authorName: String?,
    val authorUrl: String?,
    val formats: List<Format>
)
