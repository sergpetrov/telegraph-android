package com.telex.presentation.page.options

import android.content.Context
import android.content.Intent
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.telex.R
import com.telex.presentation.statistics.PageStatisticsActivity

/**
 * @author Sergey Petrov
 */
class PageOptionsDelegate(
    override val context: Context,
    override val pagePath: String
) : BasePageOptionsDelegate(context, pagePath) {

    override fun openPageStatistics() {
        val intent = Intent(context, PageStatisticsActivity::class.java)
        intent.putExtra(PageStatisticsActivity.PAGE_PATH, pagePath)
        context.startActivity(intent)
    }

    override fun deletePost(onDeleteClick: () -> Unit) {
        val builder = MaterialAlertDialogBuilder(context)
        builder.setTitle(context.getString(R.string.delete_post) + "?")
        builder.setMessage(R.string.delete_post_description)
        builder.setPositiveButton(context.getString(R.string.delete)) { _, _ -> onDeleteClick.invoke() }
        builder.setNegativeButton(context.getString(R.string.cancel)) { dialog, _ -> dialog.dismiss() }
        builder.create().show()
    }
}
