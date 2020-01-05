package com.telex.presentation.home

import android.view.View
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.recyclerview.widget.RecyclerView
import androidx.transition.ChangeBounds
import androidx.transition.TransitionManager
import com.telex.R
import com.telex.analytics.AnalyticsHelper
import com.telex.extention.isVisible
import com.telex.extention.setGone
import com.telex.model.source.remote.data.TopBannerData
import com.telex.presentation.base.BaseActivity
import com.telex.utils.ViewUtils
import kotlinx.android.synthetic.main.layout_top_banner.view.*

/**
 * @author Sergey Petrov
 */
abstract class BaseTopBannerDelegate(
    protected val activity: BaseActivity,
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
                        ViewUtils.openUrl(activity, action.url, onError = { message -> activity.showError(message) })
                    }
                }
                banner.secondAction?.let { action ->
                    secondActionButton.setGone(false)
                    secondActionButton.text = action.title
                    secondActionButton.setOnClickListener {
                        AnalyticsHelper.logTopBannerActionClick(action.title)
                        ViewUtils.openUrl(activity, action.url, onError = { message -> activity.showError(message) })
                        if (action.url.isNullOrEmpty()) {
                            showDefaultOnSecondActionClicked()
                        }
                    }
                }
            }
            val recyclerView = activity.findViewById<RecyclerView>(R.id.recyclerView)
            if (recyclerView.scrollState == RecyclerView.SCROLL_STATE_IDLE) {
                TransitionManager.beginDelayedTransition(coordinatorLayout, ChangeBounds())
            }
        }
    }

    fun hideBanner(coordinatorLayout: CoordinatorLayout, bannerView: View) {
        if (bannerView.isVisible) {
            bannerView.setGone(true)
            val recyclerView = activity.findViewById<RecyclerView>(R.id.recyclerView)
            if (recyclerView.scrollState == RecyclerView.SCROLL_STATE_IDLE) {
                TransitionManager.beginDelayedTransition(coordinatorLayout, ChangeBounds())
            }
        }
    }
}
