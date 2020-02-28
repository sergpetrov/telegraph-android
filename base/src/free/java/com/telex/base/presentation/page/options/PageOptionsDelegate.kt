package com.telex.base.presentation.page.options

import android.content.Context
import android.content.Intent
import com.telex.base.presentation.home.UpgradeToProActivity

/**
 * @author Sergey Petrov
 */
class PageOptionsDelegate(
    override val context: Context,
    override val pagePath: String
) : BasePageOptionsDelegate(context, pagePath) {

    override fun openPageStatistics() {
        context.startActivity(Intent(context, UpgradeToProActivity::class.java))
    }

    override fun deletePost(onDeleteClick: () -> Unit) {
        context.startActivity(Intent(context, UpgradeToProActivity::class.java))
    }
}
