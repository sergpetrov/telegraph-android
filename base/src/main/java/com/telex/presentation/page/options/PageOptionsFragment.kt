package com.telex.presentation.page.options

import android.app.Activity
import android.app.Dialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import com.telex.R
import com.telex.analytics.AnalyticsHelper
import com.telex.di.Scopes
import com.telex.model.system.ServerManager
import com.telex.presentation.base.BaseOptionsFragment
import com.telex.presentation.page.EditorMode
import com.telex.utils.ViewUtils.Companion.copyToClipboard
import moxy.presenter.InjectPresenter
import moxy.presenter.ProvidePresenter
import toothpick.Toothpick

/**
 * @author Sergey Petrov
 */
class PageOptionsFragment : BaseOptionsFragment(), PageOptionsView {

    private val mode: EditorMode by lazy {
        arguments?.getSerializable(MODE) as? EditorMode ?: throw IllegalArgumentException("mode can't be null")
    }
    private val pageId: Long by lazy {
        arguments?.getLong(PAGE_ID) ?: throw IllegalArgumentException("pageId can't be null")
    }
    private val pagePath: String by lazy {
        arguments?.getString(PAGE_PATH) ?: throw IllegalArgumentException("pagePath can't be null")
    }
    private val draft: Boolean by lazy {
        arguments?.getBoolean(PAGE_IS_DRAFT) ?: throw IllegalArgumentException("draft can't be null")
    }
    private val pageUrl: String
        get() = "${ServerManager.endPoint}/$pagePath"

    @InjectPresenter
    lateinit var presenter: PageOptionsPresenter

    @ProvidePresenter
    fun providePresenter() = Toothpick.openScope(Scopes.App).getInstance(PageOptionsPresenter::class.java)

    private val pageOptionsDelegate by lazy {
        PageOptionsDelegate(
                context = context,
                pagePath = pagePath)
    }

    val editOption = Option(R.drawable.ic_edit, R.string.edit)
    val publishOption = Option(R.drawable.ic_done, R.string.publish)

    private val discardDraftOption = Option(R.drawable.ic_discard, R.string.discard_draft, color = R.color.error,
            onClick = {
                presenter.discardDraft(pageId)
                onDraftDiscardedListener?.invoke()
                if (mode == EditorMode.Edit) {
                    (context as Activity).finish()
                }
            }
    )

    private val deletePostOption = Option(R.drawable.ic_delete, R.string.delete_post, color = R.color.error,
            onClick = {
                AnalyticsHelper.logClickDeletePost()
                pageOptionsDelegate.deletePost(onDeleteClick = {
                    presenter.deletePost(pageId)
                    onOnPostDeletedListener?.invoke()
                })
            }
    )

    var onDraftDiscardedListener: (() -> Unit)? = null
    var onOnPostDeletedListener: (() -> Unit)? = null

    private val openOption = Option(R.drawable.ic_open_in_app, R.string.open,
            onClick = {
                AnalyticsHelper.logOpenPageInBrowser()
                startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(pageUrl)))
            }
    )
    private val statisticsOption = Option(R.drawable.ic_chart, R.string.statistics,
            onClick = {
                AnalyticsHelper.logOpenPageStatistics()
                pageOptionsDelegate.openPageStatistics()
            }
    )
    private val copyLinkOption = Option(R.drawable.ic_copy, R.string.copy_link,
            onClick = {
                AnalyticsHelper.logCopyPageLink()
                copyToClipboard(context, pageUrl)
            }
    )
    private val shareOption = Option(R.drawable.ic_share, R.string.share,
            onClick = {
                AnalyticsHelper.logSharePage()
                val sharingIntent = Intent(Intent.ACTION_SEND)
                sharingIntent.type = "text/plain"
                sharingIntent.putExtra(Intent.EXTRA_TEXT, pageUrl)
                startActivity(Intent.createChooser(sharingIntent, getString(R.string.share_using)))
            }
    )

    override fun setupView(dialog: Dialog) {
        super.setupView(dialog)

        if (mode == EditorMode.View) {
            addOption(editOption)
        } else if (mode == EditorMode.Edit) {
            addOption(publishOption)
        }

        if (draft) {
            addOption(discardDraftOption)
        } else {
            addOptions(
                    openOption,
                    statisticsOption,
                    copyLinkOption,
                    shareOption,
                    deletePostOption
            )
        }
    }

    override fun showProgress(isVisible: Boolean) {}

    companion object {
        private const val MODE = "MODE"
        private const val PAGE_ID = "PAGE_ID"
        private const val PAGE_PATH = "PAGE_PATH"
        private const val PAGE_IS_DRAFT = "PAGE_IS_DRAFT"

        fun newInstance(
            mode: EditorMode,
            pageId: Long,
            pagePath: String?,
            draft: Boolean
        ) = PageOptionsFragment().apply {
            arguments = Bundle().apply {
                putSerializable(MODE, mode)
                putLong(PAGE_ID, pageId)
                putString(PAGE_PATH, pagePath)
                putBoolean(PAGE_IS_DRAFT, draft)
            }
        }
    }
}
