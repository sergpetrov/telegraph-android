package com.telex.presentation.page.options

import android.content.Context

/**
 * @author Sergey Petrov
 */
class PageOptionsDelegate(
    override val context: Context,
    override val pagePath: String
) : BasePageOptionsDelegate(context, pagePath) {

    override fun openPageStatistics() {
    }

    override fun deletePost(onDeleteClick: () -> Unit) {
    }
}
