package com.telex.presentation.home

import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import com.telex.R
import com.telex.extention.getColorFromAttr
import com.telex.model.source.local.entity.Page
import com.telex.model.source.remote.data.TopBannerData
import com.telex.presentation.base.BaseActivity
import com.telex.presentation.base.BaseFragment
import com.telex.presentation.page.EditorMode
import kotlinx.android.synthetic.main.fragment_pages.*
import kotlinx.android.synthetic.main.layout_no_stories.view.*
import moxy.presenter.InjectPresenter
import moxy.presenter.ProvidePresenter

/**
 * @author Sergey Petrov
 */
class PagesFragment : BaseFragment(), PagesView {

    override val layoutRes: Int = R.layout.fragment_pages

    private val drafts by lazy {
        arguments?.getBoolean(EXTRA_DRAFTS) ?: throw IllegalArgumentException("drafts can't be null")
    }

    private lateinit var pagesAdapter: PagesAdapter
    private var topBannerDelegate: TopBannerDelegate? = null

    @InjectPresenter
    lateinit var presenter: PagesPresenter

    @ProvidePresenter
    fun providePresenter(): PagesPresenter {
        return scope.getInstance(PagesPresenter::class.java).also { it.drafts = drafts }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        pagesAdapter = PagesAdapter(context,
                onItemClickListener = { page: Page, mode: EditorMode ->
                    if (!swipeRefreshLayout.isRefreshing) {
                        presenter.showNewPageActivity(context, page, mode)
                    }
                },
                onNextPageListener = {
                    if (!drafts) {
                        presenter.loadMorePages()
                    }
                }
        )

        recyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = pagesAdapter
            itemAnimator = null
        }

        swipeRefreshLayout.setProgressBackgroundColorSchemeColor(context.getColorFromAttr(R.attr.colorPrimaryDark))
        swipeRefreshLayout.setColorSchemeColors(context.getColorFromAttr(R.attr.colorAccent))

        swipeRefreshLayout.isEnabled = !drafts
        swipeRefreshLayout.setOnRefreshListener {
            presenter.refreshUserAndPages()
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
        if (drafts) {
            view.titleTextView.text = getString(R.string.no_drafts)
            view.subTitleTextView.text = getString(R.string.drafts_are_storing_on_device_only)
        } else {
            view.titleTextView.text = getString(R.string.no_posts)
            view.subTitleTextView.text = getString(R.string.click_button_to_create_a_new_post)
        }
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

    companion object {
        const val EXTRA_DRAFTS = "EXTRA_DRAFTS"

        fun newInstance(drafts: Boolean): PagesFragment {
            val fragment = PagesFragment()
            val bundle = Bundle()
            bundle.putBoolean(EXTRA_DRAFTS, drafts)
            fragment.arguments = bundle
            return fragment
        }
    }
}
