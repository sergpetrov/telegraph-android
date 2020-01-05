package com.telex.presentation.page.options

import android.content.Context

/**
 * @author Sergey Petrov
 */
abstract class BasePageOptionsDelegate(
    protected open val context: Context,
    protected open val pagePath: String
) {
    abstract fun openPageStatistics()
    abstract fun deletePost(onDeleteClick: () -> Unit)
}
