package com.telex.base.presentation.pages

import android.os.Bundle
import android.view.View
import androidx.core.view.doOnPreDraw
import androidx.navigation.fragment.FragmentNavigatorExtras
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.transition.Hold
import com.telex.base.R
import com.telex.base.extention.applySystemWindowInsetsPadding
import com.telex.base.extention.getColorFromAttr
import com.telex.base.model.source.local.entity.Page
import com.telex.base.model.source.remote.data.TopBannerData
import com.telex.base.presentation.base.BaseActivity
import com.telex.base.presentation.base.BaseFragment
import com.telex.base.presentation.home.TopBannerDelegate
import com.telex.base.presentation.page.EditorMode
import kotlinx.android.synthetic.main.fragment_pages.*
import kotlinx.android.synthetic.main.layout_no_stories.view.*
import moxy.presenter.InjectPresenter
import moxy.presenter.ProvidePresenter

/**
 * @author Sergey Petrov
 */
open class PagesFragment : BaseFragment(), PagesView {

    override val layoutRes: Int
        get() = R.layout.fragment_pages

    private lateinit var pagesAdapter: PagesAdapter

    private var topBannerDelegate: TopBannerDelegate? = null

    @InjectPresenter
    lateinit var presenter: PagesPresenter

    @ProvidePresenter
    fun providePresenter(): PagesPresenter {
        return scope.getInstance(PagesPresenter::class.java)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // Postpone enter transitions to allow shared element transitions to run.
        // https://github.com/googlesamples/android-architecture-components/issues/495
        postponeEnterTransition()
        view.doOnPreDraw { startPostponedEnterTransition() }

        pagesAdapter = PagesAdapter(context,
                onItemClickListener = { itemView: View, page: Page, mode: EditorMode ->
                    if (!swipeRefreshLayout.isRefreshing) {
                        val extras = FragmentNavigatorExtras(itemView to itemView.transitionName)
                        findNavController().navigate(
                                PagesFragmentDirections.openPageEditorAction(
                                        mode = mode,
                                        pageId = page.id,
                                        title = page.title,
                                        authorName = page.authorName,
                                        authorUrl = page.authorUrl
                                ),
                                extras
                        )
                    }
                },
                onNextPageListener = { presenter.loadMorePages() }
        )

        pagesRecyclerView.apply {
            applySystemWindowInsetsPadding(applyTop = true, applyBottom = true)
            layoutManager = LinearLayoutManager(context)
            adapter = pagesAdapter
            itemAnimator = null
        }

        swipeRefreshLayout.apply {
            setSlingshotDistance(context.resources.getDimensionPixelOffset(R.dimen.swipe_slingshot_distance))
            setProgressViewOffset(true, 0, context.resources.getDimensionPixelOffset(R.dimen.swipe_progress_offset))
            setProgressBackgroundColorSchemeColor(context.getColorFromAttr(R.attr.colorPrimaryDark))
            setColorSchemeColors(context.getColorFromAttr(R.attr.colorAccent))

            setOnRefreshListener {
                presenter.refreshUserAndPages()
            }
        }
    }

    override fun showTopBanner(banner: TopBannerData) {
        topBannerDelegate = TopBannerDelegate(activity as BaseActivity, banner)
        topBannerDelegate?.showBanner(coordinatorLayout, topBannerLayout)
    }

    override fun hideTopBanner() {
        topBannerDelegate?.hideBanner(coordinatorLayout, topBannerLayout)
    }

    override fun showPages(pages: List<Page>, hasMore: Boolean) {
        pagesAdapter.setData(pages, hasMore)
    }

    override fun showEmpty() {
        val view = View.inflate(context, R.layout.layout_no_stories, null)
        view.titleTextView.text = getString(R.string.no_posts)
        view.subTitleTextView.text = getString(R.string.click_button_to_create_a_new_post)
        messageLayout.addView(view)
    }

    override fun hideEmpty() {
        messageLayout.removeAllViews()
    }

    override fun showProgress(isVisible: Boolean) {
        swipeRefreshLayout.isRefreshing = isVisible
    }

    override fun showAdapterProgress() {
        pagesAdapter.showProgress()
    }

    override fun hideAdapterProgress() {
        pagesAdapter.hideProgress()
    }
}