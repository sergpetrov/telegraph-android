package com.telex.base.presentation.pages

import android.os.Bundle
import android.view.View
import androidx.core.view.doOnPreDraw
import androidx.navigation.fragment.FragmentNavigatorExtras
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.telex.base.R
import com.telex.base.extention.applySystemWindowInsetsPadding
import com.telex.base.model.source.local.entity.Page
import com.telex.base.presentation.base.BaseFragment
import com.telex.base.presentation.page.EditorMode
import kotlinx.android.synthetic.main.fragment_drafts.*
import kotlinx.android.synthetic.main.layout_no_stories.view.*
import moxy.presenter.InjectPresenter
import moxy.presenter.ProvidePresenter

/**
 * @author Sergey Petrov
 */
class DraftsFragment : BaseFragment(), DraftsView {

    override val layoutRes: Int
        get() = R.layout.fragment_drafts

    private lateinit var pagesAdapter: PagesAdapter

    @InjectPresenter
    lateinit var presenter: DraftsPresenter

    @ProvidePresenter
    fun providePresenter(): DraftsPresenter {
        return scope.getInstance(DraftsPresenter::class.java)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // Postpone enter transitions to allow shared element transitions to run.
        // https://github.com/googlesamples/android-architecture-components/issues/495
        postponeEnterTransition()
        view.doOnPreDraw { startPostponedEnterTransition() }

        pagesAdapter = PagesAdapter(context,
                onItemClickListener = { itemView: View, page: Page, mode: EditorMode ->
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
                },
                onNextPageListener = null
        )

        pagesRecyclerView.apply {
            applySystemWindowInsetsPadding(applyLeft = false, applyTop = true, applyRight = false, applyBottom = true)
            layoutManager = LinearLayoutManager(context)
            adapter = pagesAdapter
            itemAnimator = null
        }
    }

    override fun showPages(pages: List<Page>, hasMore: Boolean) {
        pagesAdapter.setData(pages, hasMore)
    }

    override fun showEmpty() {
        val view = View.inflate(context, R.layout.layout_no_stories, null)
        view.titleTextView.text = getString(R.string.no_drafts)
        view.subTitleTextView.text = getString(R.string.drafts_are_storing_on_device_only)
        messageLayout.addView(view)
    }

    override fun hideEmpty() {
        messageLayout.removeAllViews()
    }
}