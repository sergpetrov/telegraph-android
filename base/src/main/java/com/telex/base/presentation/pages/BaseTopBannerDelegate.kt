package com.telex.base.presentation.pages

import android.view.View
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.recyclerview.widget.RecyclerView
import androidx.transition.ChangeBounds
import androidx.transition.TransitionManager
import com.telex.base.R
import com.telex.base.analytics.AnalyticsHelper
import com.telex.base.extention.isVisible
import com.telex.base.extention.setGone
import com.telex.base.model.source.remote.data.TopBannerData
import com.telex.base.utils.ViewUtils
import kotlinx.android.synthetic.main.layout_top_banner.view.*

/**
 * @author Sergey Petrov
 */
abstract class BaseTopBannerDelegate(
        protected val fragment: PagesFragment?,
        protected val banner: TopBannerData
) {

    abstract val enabled: Boolean

    abstract fun showDefaultOnSecondActionClicked()

    fun showBanner(coordinatorLayout: CoordinatorLayout, bannerView: View) {
        if (enabled && !bannerView.isVisible) {
            bannerView.setGone(false)
            with(bannerView) {
                messageTextView.text = banner.message
                banner.firstAction?.let { action ->
                    firstActionButton.setGone(false)
                    firstActionButton.text = action.title
                    firstActionButton.setOnClickListener {
                        AnalyticsHelper.logTopBannerActionClick(action.title)
                        ViewUtils.openUrl(context, action.url, onError = { message -> fragment?.showError(message) })
                    }
                }
                banner.secondAction?.let { action ->
                    secondActionButton.setGone(false)
                    secondActionButton.text = action.title
                    secondActionButton.setOnClickListener {
                        AnalyticsHelper.logTopBannerActionClick(action.title)
                        if (fragment != null) {
                            ViewUtils.openUrl(fragment.context, action.url, onError = { message -> fragment.showError(message) })
                        }
                        if (action.url.isNullOrEmpty()) {
                            showDefaultOnSecondActionClicked()
                        }
                    }
                }
            }
            val recyclerView = fragment?.view?.findViewById<RecyclerView>(R.id.pagesRecyclerView)
            if (recyclerView?.scrollState == RecyclerView.SCROLL_STATE_IDLE) {
                TransitionManager.beginDelayedTransition(coordinatorLayout, ChangeBounds())
            }
        }
    }

    fun hideBanner(coordinatorLayout: CoordinatorLayout, bannerView: View) {
        if (bannerView.isVisible) {
            bannerView.setGone(true)
            val recyclerView = fragment?.view?.findViewById<RecyclerView>(R.id.pagesRecyclerView)
            if (recyclerView?.scrollState == RecyclerView.SCROLL_STATE_IDLE) {
                TransitionManager.beginDelayedTransition(coordinatorLayout, ChangeBounds())
            }
        }
    }
}
