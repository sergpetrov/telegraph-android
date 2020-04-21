package com.telex.base.presentation.page

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.transition.Slide
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import android.widget.LinearLayout
import androidx.appcompat.widget.TooltipCompat
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.transition.MaterialContainerTransform
import com.telex.base.R
import com.telex.base.analytics.AnalyticsHelper
import com.telex.base.extention.applySystemWindowInsetsPadding
import com.telex.base.extention.disable
import com.telex.base.extention.enable
import com.telex.base.extention.getColorFromAttr
import com.telex.base.extention.setGone
import com.telex.base.extention.showKeyboard
import com.telex.base.extention.themeInterpolator
import com.telex.base.model.source.local.entity.Page
import com.telex.base.presentation.base.BaseFragment
import com.telex.base.presentation.page.adapter.FormatAdapter
import com.telex.base.presentation.page.dialogs.AuthorDialogFragment
import com.telex.base.presentation.page.dialogs.InsertIframeDialogFragment
import com.telex.base.presentation.page.dialogs.InsertImageUrlCaptionDialogFragment
import com.telex.base.presentation.page.format.Format
import com.telex.base.presentation.page.format.FormatType
import com.telex.base.presentation.page.format.ImageFormat
import com.telex.base.presentation.page.options.InsertImageOptionsFragment
import com.telex.base.presentation.page.options.PageOptionsFragment
import com.telex.base.utils.PermissionsHelper
import kotlinx.android.synthetic.main.fragment_page_editor.*
import kotlinx.android.synthetic.main.layout_editor_toolbar.*
import kotlinx.android.synthetic.main.layout_editor_toolbar.view.*
import moxy.MvpAppCompatActivity
import moxy.presenter.InjectPresenter
import moxy.presenter.ProvidePresenter

/**
 * @author Sergey Petrov
 */
class PageEditorFragment : BaseFragment(), PageEditorView {

    override val layoutRes: Int
        get() = R.layout.fragment_page_editor

    private val args: PageEditorFragmentArgs by navArgs()

    private val pageId: Long? by lazy { if (args.pageId != -1L) args.pageId else null }
    private val mode: EditorMode by lazy { args.mode }
    private val title: String? by lazy { args.title }
    private var authorName: String? = null
    private var authorUrl: String? = null

    private val formatAdapter: FormatAdapter by lazy {
        FormatAdapter(
                onFocusItemChanged = { format -> onFocusItemChanged(format) },
                onTextSelected = this::onTextSelected,
                onItemChanged = { onDraftChanged() },
                onPaste = { html -> presenter.convertHtml(html) }
        )
    }

    private var formatAdapterDataObserver: RecyclerView.AdapterDataObserver? = null
    private val addImageFromStorageDelegate by lazy { AddImageFromStorageDelegate(context) }

    @InjectPresenter
    lateinit var presenter: PageEditorPresenter

    @ProvidePresenter
    fun providePresenter(): PageEditorPresenter {
        return scope.getInstance(PageEditorPresenter::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        prepareTransitions()

        authorName = args.authorName
        authorUrl = args.authorUrl

        presenter.openPage(pageId)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (pageId != null) {
            constraintLayout.transitionName = getString(R.string.page_item_view_shared_element, pageId.toString())
        }

        coordinatorLayout.applySystemWindowInsetsPadding(applyTop = true)
        editorToolbar.applySystemWindowInsetsPadding(applyBottom = true)

        with(recyclerView) {
            val linearLayoutManager = LinearLayoutManager(context)
            layoutManager = linearLayoutManager
            adapter = formatAdapter
            setItemViewCacheSize(100)
            itemAnimator = null
            val gestureDetector = GestureDetector(context, object : GestureDetector.SimpleOnGestureListener() {
                override fun onSingleTapUp(e: MotionEvent?): Boolean {
                    val lastItem = formatAdapter.items.lastOrNull()
                    if (lastItem == null || lastItem.html != Format.getEmptyHtml(lastItem.type) || lastItem.type == FormatType.HORIZONTAL_RULE) {
                        formatAdapter.addBlockFormatItem(formatAdapter.items.size + 1, Format(FormatType.PARAGRAPH))
                    } else {
                        formatAdapter.requestFocusForFormatItem(lastItem)
                    }
                    recyclerView?.postDelayed({ activity?.showKeyboard() }, 50)
                    return super.onSingleTapUp(e)
                }
            })
            setOnTouchListener { _, event -> gestureDetector.onTouchEvent(event) }
        }

        val callBack = object : ItemTouchHelper.Callback() {

            override fun isLongPressDragEnabled(): Boolean {
                return false
            }

            override fun getMovementFlags(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder): Int {
                val dragFlags = ItemTouchHelper.UP or ItemTouchHelper.DOWN
                val swipeFlags = 0 // ItemTouchHelper.START | ItemTouchHelper.END
                return makeMovementFlags(dragFlags, swipeFlags)
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
            }

            override fun onMove(recyclerView: RecyclerView, source: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder): Boolean {
                val fromPosition = source.adapterPosition
                val toPosition = target.adapterPosition

                if (source.itemViewType != FormatAdapter.TYPE_HEADER && toPosition != 0) {
                    formatAdapter.moveBlockFormatItem(fromPosition, toPosition)
                    return true
                }

                return false
            }
        }

        val itemTouchHelper = ItemTouchHelper(callBack)
        itemTouchHelper.attachToRecyclerView(recyclerView)

        formatAdapter.itemTouchHelper = itemTouchHelper
        if (formatAdapterDataObserver != null) {
            formatAdapter.unregisterAdapterDataObserver(requireNotNull(formatAdapterDataObserver))
        } else {
            formatAdapterDataObserver = createFormatAdapterDataObserver()
        }
        formatAdapter.registerAdapterDataObserver(requireNotNull(formatAdapterDataObserver))

        doneImageView.setOnClickListener { doneOnClicked() }
        closeImageView.setOnClickListener { findNavController().popBackStack() }

        moreImageView.setOnClickListener { presenter.onMoreClicked() }

        if (!title.isNullOrBlank()) {
            formatAdapter.pageTitle = title
        }

        setupEditorToolbar()
        setupMode(mode)
        setupAuthor()

        startTransitions()
    }

    private fun onDraftChanged() {
        presenter.onDraftChanged(
                DraftFields(
                        title = getPageTitle(),
                        authorName = authorName,
                        authorUrl = authorUrl,
                        formats = formatAdapter.items
                )
        )
    }

    override fun showMore(page: Page) {
        PageOptionsFragment.newInstance(
                mode = EditorMode.Edit,
                pageId = page.id,
                pagePath = page.path,
                draft = page.draft
        ).also {
            it.publishOption.onClick = { doneOnClicked() }
            it.onDraftDiscardedListener = { presenter.isDraftNeeded = false }
            it.onOnPostDeletedListener = { findNavController().popBackStack() }
        }.show(parentFragmentManager)
    }

    override fun onViewStateRestored(savedInstanceState: Bundle?) {
        super.onViewStateRestored(savedInstanceState)
        savedInstanceState?.let {
            authorName = savedInstanceState.getString(PAGE_AUTHOR_NAME)
            authorUrl = savedInstanceState.getString(PAGE_AUTHOR_URL)
            recyclerView.layoutManager?.onRestoreInstanceState(savedInstanceState.getParcelable(PAGE_ITEMS))
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString(PAGE_AUTHOR_NAME, authorName)
        outState.putString(PAGE_AUTHOR_URL, authorUrl)
        outState.putParcelable(PAGE_ITEMS, recyclerView.layoutManager?.onSaveInstanceState())
    }

    override fun onDestroy() {
        super.onDestroy()
        presenter.discardDraftPageIfNeeded(getPageTitle(), formatAdapter.items)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (MvpAppCompatActivity.RESULT_OK == resultCode && data != null) {

            when (requestCode) {
                REQUEST_INSERT_IMAGE -> {
                    val images = addImageFromStorageDelegate.convertIntentDataToImageFormats(context, data)

                    images.forEach { image ->
                        addBlockFormatItem(image)
                    }
                }
            }
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        when (requestCode) {
            REQUEST_INSERT_IMAGE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    chooseImage(requestCode)
                }
            }
            else -> {
                super.onRequestPermissionsResult(requestCode, permissions, grantResults)
            }
        }
    }

    override fun showProgress(isVisible: Boolean) {
        if (isVisible) {
            showOverlay()
        } else {
            hideOverlay()
        }
    }

    override fun onPageSaved() {
        findNavController().popBackStack()
    }

    override fun showPage(page: Page, formats: List<Format>) {
        with(formatAdapter) {
            pageTitle = page.title
            submitList(formats)
            focusedItem = null
            recyclerView.scrollToPosition(0)
        }
    }

    private fun setupEditorToolbar() {
        with(editorToolbar) {
            boldFormatButton.setOnClickListener { formatAdapter.toggleFormat(FormatType.STRONG) }
            boldFormatButton.tooltipText = getString(R.string.format_bold)

            italicFormatButton.setOnClickListener { formatAdapter.toggleFormat(FormatType.ITALIC) }
            italicFormatButton.tooltipText = getString(R.string.format_italic)

            underlineFormatButton.setOnClickListener { formatAdapter.toggleFormat(FormatType.UNDERLINE) }
            underlineFormatButton.tooltipText = getString(R.string.format_underline)

            strikethroughFormatButton.setOnClickListener { formatAdapter.toggleFormat(FormatType.STRIKETHROUGH) }
            strikethroughFormatButton.tooltipText = getString(R.string.format_strike)

            linkButton.setOnClickListener {
                linkButton.isChecked = !linkButton.isChecked
                formatAdapter.toggleFormat(FormatType.LINK)
            }
            linkButton.tooltipText = getString(R.string.format_link)

            paragraphButton.setOnClickListener { formatAdapter.toggleFormat(FormatType.PARAGRAPH) }
            paragraphButton.tooltipText = getString(R.string.format_paragraph)

            quoteFormatButton.setOnClickListener { formatAdapter.toggleFormat(FormatType.QUOTE) }
            quoteFormatButton.tooltipText = getString(R.string.format_block_quote)

            insertLineButton.setOnClickListener { formatAdapter.addBlockFormatItem(Format(FormatType.HORIZONTAL_RULE)) }
            TooltipCompat.setTooltipText(insertLineButton, getString(R.string.format_hr))

            headingFormatButton.setOnClickListener { formatAdapter.toggleFormat(FormatType.HEADING) }
            headingFormatButton.tooltipText = getString(R.string.format_heading)

            subHeadingFormatButton.setOnClickListener { formatAdapter.toggleFormat(FormatType.SUB_HEADING) }
            subHeadingFormatButton.tooltipText = getString(R.string.format_subheading)

            unorderedListFormatButton.setOnClickListener { formatAdapter.toggleFormat(FormatType.UNORDERED_LIST) }
            unorderedListFormatButton.tooltipText = getString(R.string.format_list_unordered)

            orderedListFormatButton.setOnClickListener { formatAdapter.toggleFormat(FormatType.ORDERED_LIST) }
            orderedListFormatButton.tooltipText = getString(R.string.format_list_ordered)

            insertImageButton.setOnClickListener {
                InsertImageOptionsFragment().apply {
                    fromGalleryOption.onClick = {
                        addImageFromStorageDelegate.showAlert(endAction = { chooseImage(REQUEST_INSERT_IMAGE) })
                    }
                    byUrlOption.onClick = { showImageUrlCaptionDialog(null) }
                }.show(parentFragmentManager)
            }
            TooltipCompat.setTooltipText(insertImageButton, getString(R.string.insert_image))

            insertIframeButton.setOnClickListener {
                InsertIframeDialogFragment().apply {
                    onAddClickListener = { format -> addBlockFormatItem(format) }
                }.show(parentFragmentManager)
            }
            TooltipCompat.setTooltipText(insertIframeButton, getString(R.string.format_embed))

            moveUpButton.setOnClickListener {
                AnalyticsHelper.logMoveBlockUp()

                val focusedItemPosition = formatAdapter.getPositionForFocusedItem()
                val toPosition = focusedItemPosition - 1
                if (toPosition > 0) {
                    formatAdapter.moveBlockFormatItem(focusedItemPosition, toPosition)
                }
            }
            TooltipCompat.setTooltipText(moveUpButton, getString(R.string.format_block_move_up))

            moveDownButton.setOnClickListener {
                AnalyticsHelper.logMoveBlockDown()

                val focusedItemPosition = formatAdapter.getPositionForFocusedItem()
                val toPosition = focusedItemPosition + 1
                if (toPosition > 0 && toPosition < formatAdapter.itemCount) {
                    formatAdapter.moveBlockFormatItem(focusedItemPosition, toPosition)
                }
            }
            TooltipCompat.setTooltipText(moveDownButton, getString(R.string.format_block_move_down))
        }

        onTextSelected()
    }

    private fun doneOnClicked() {
        if (isInputValid()) {
            val builder = MaterialAlertDialogBuilder(context)
            builder.setMessage(R.string.do_you_want_publish)
            builder.setPositiveButton(getString(R.string.publish)) { _, _ ->
                presenter.publishPage(getPageTitle(), authorName, authorUrl, formatAdapter.items)
            }

            builder.setNegativeButton(getString(R.string.save_draft)) { dialog, _ ->
                presenter.savePageDraftIfNeeded(
                        draftFields = DraftFields(
                                title = getPageTitle(),
                                authorName = authorName,
                                authorUrl = authorUrl,
                                formats = formatAdapter.items
                        ),
                        force = true
                )
                dialog.dismiss()
            }
            builder.create().show()
        }
    }

    private fun isInputValid(): Boolean {
        val isValid: Boolean
        val pageHeaderViewHolder = formatAdapter.getPageHeaderViewHolder()
        if (pageHeaderViewHolder == null) {
            if (!formatAdapter.isPageTitleValid()) {
                formatAdapter.focusedItem = null
                recyclerView.layoutManager?.scrollToPosition(0)
                recyclerView.postDelayed({ formatAdapter.getPageHeaderViewHolder()?.isValid() }, 200)

                isValid = false
            } else {
                isValid = true
            }
        } else {
            isValid = pageHeaderViewHolder.isValid()
        }
        return isValid
    }

    private fun getPageTitle(): String {
        return formatAdapter.pageTitle.toString()
    }

    private fun setupMode(mode: EditorMode) {
        if (mode == EditorMode.Edit) {
            moreImageView.visibility = View.VISIBLE
        } else {
            (doneImageView.layoutParams as LinearLayout.LayoutParams).setMargins(0, 0, resources.getDimensionPixelSize(R.dimen.margin_8), 0)
            moreImageView.visibility = View.GONE
        }
    }

    private fun setupAuthor() {
        setupAuthorName(authorName)
        authorLayout.setOnClickListener {
            AuthorDialogFragment.newInstance(
                    authorName = authorName,
                    authorUrl = authorUrl,
                    onAddClickListener = { authorName, authorUrl ->
                        this.authorName = authorName
                        this.authorUrl = authorUrl

                        setupAuthorName(authorName)
                    }
            ).show(parentFragmentManager)
        }
    }

    private fun setupAuthorName(authorName: String?) {
        if (authorName.isNullOrBlank()) {
            authorNameTextView.text = getString(R.string.add_name)
        } else {
            authorNameTextView.text = authorName
            authorNameTextView.setTextColor(context.getColorFromAttr(R.attr.colorAccent))
        }
    }

    private fun chooseImage(requestCode: Int) {
        if (PermissionsHelper.checkPermissions(requireActivity(), requestCode, Manifest.permission.READ_EXTERNAL_STORAGE)) {
            addImageFromStorageDelegate.startActivityForResult(context, requestCode)
        }
    }

    private fun showImageUrlCaptionDialog(url: String?) {
        InsertImageUrlCaptionDialogFragment.newInstance(url,
                onAddClickListener = { imageUrl, caption -> addBlockFormatItem(ImageFormat(imageUrl, caption)) }
        ).show(parentFragmentManager)
    }

    private fun addBlockFormatItem(format: Format) {
        formatAdapter.addBlockFormatItem(format)
    }

    private fun onTextSelected(selected: Boolean = false, format: Format? = null, formatTypes: List<FormatType> = emptyList()) {
        with(editorToolbar) {
            boldFormatButton.isChecked = formatTypes.contains(FormatType.BOLD)
            italicFormatButton.isChecked = formatTypes.contains(FormatType.ITALIC)
            strikethroughFormatButton.isChecked = formatTypes.contains(FormatType.STRIKETHROUGH)
            underlineFormatButton.isChecked = formatTypes.contains(FormatType.UNDERLINE)
            linkButton.isChecked = formatTypes.contains(FormatType.LINK)

            boldFormatButton.setGone(!selected)
            italicFormatButton.setGone(!selected)
            strikethroughFormatButton.setGone(!selected)
            underlineFormatButton.setGone(!selected)
            linkButton.setGone(!selected)

            paragraphButton.setGone(selected)
            headingFormatButton.setGone(selected)
            subHeadingFormatButton.setGone(selected)
            quoteFormatButton.setGone(selected)
            unorderedListFormatButton.setGone(selected)
            orderedListFormatButton.setGone(selected)
            insertImageButton.setGone(selected)
            insertLineButton.setGone(selected)
            insertIframeButton.setGone(selected)
            moveUpButton.setGone(selected)
            moveDownButton.setGone(selected)

            if (format != null) {
                italicFormatButton.enable()
                boldFormatButton.enable()
                italicFormatButton.enable()
                strikethroughFormatButton.enable()
                underlineFormatButton.enable()

                when (format.type) {
                    FormatType.QUOTE, FormatType.ASIDE -> italicFormatButton.disable()
                }
            }
        }
    }

    private fun onFocusItemChanged(format: Format?) {
        enableEditorToolbar(format != null || formatAdapter.items.isEmpty())

        paragraphButton.isChecked = format?.type == FormatType.PARAGRAPH
        headingFormatButton.isChecked = format?.type == FormatType.HEADING
        subHeadingFormatButton.isChecked = format?.type == FormatType.SUB_HEADING
        quoteFormatButton.isChecked = format?.type == FormatType.QUOTE
        unorderedListFormatButton.isChecked = format?.type == FormatType.UNORDERED_LIST
        orderedListFormatButton.isChecked = format?.type == FormatType.ORDERED_LIST
    }

    private fun enableEditorToolbar(isEnabled: Boolean) {
        with(editorToolbar) {
            for (i in 0 until optionsLayout.childCount) {
                optionsLayout.getChildAt(i).run {
                    isClickable = isEnabled
                    isFocusable = isEnabled
                    if (isEnabled) {
                        enable()
                    } else {
                        disable()
                    }
                }
            }
        }
    }

    private fun createFormatAdapterDataObserver(): RecyclerView.AdapterDataObserver {
        return object : RecyclerView.AdapterDataObserver() {
            override fun onChanged() {
                super.onChanged()
                onDraftChanged()
            }

            override fun onItemRangeRemoved(positionStart: Int, itemCount: Int) {
                super.onItemRangeRemoved(positionStart, itemCount)
                onChanged()
            }

            override fun onItemRangeMoved(fromPosition: Int, toPosition: Int, itemCount: Int) {
                super.onItemRangeMoved(fromPosition, toPosition, itemCount)
                onChanged()
            }

            override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
                super.onItemRangeInserted(positionStart, itemCount)
                onChanged()
            }

            override fun onItemRangeChanged(positionStart: Int, itemCount: Int) {
                super.onItemRangeChanged(positionStart, itemCount)
                onChanged()
            }

            override fun onItemRangeChanged(positionStart: Int, itemCount: Int, payload: Any?) {
                super.onItemRangeChanged(positionStart, itemCount, payload)
                onChanged()
            }
        }
    }

    private fun prepareTransitions() {
        postponeEnterTransition()

        if (mode == EditorMode.Edit) {
            sharedElementEnterTransition = MaterialContainerTransform().apply {
                // Scope the transition to a view in the hierarchy so we know it will be added under
                // the bottom app bar but over the Hold transition from the exiting HomeFragment.
                drawingViewId = R.id.fragmentContainerView
                duration = resources.getInteger(R.integer.motion_default_large).toLong()
                interpolator = context.themeInterpolator(R.attr.motionInterpolatorPersistent)
            }
            sharedElementReturnTransition = MaterialContainerTransform().apply {
                // Again, scope the return transition so it is added below the bottom app bar.
                drawingViewId = R.id.pagesRecyclerView
                duration = resources.getInteger(R.integer.motion_default_large).toLong()
                interpolator = context.themeInterpolator(R.attr.motionInterpolatorPersistent)
            }
        }
    }

    private fun startTransitions() {
        if (mode == EditorMode.Create) {
            // Delay creating the enterTransition until after we have inflated this Fragment's binding
            // and are able to access the view to be transitioned to.
            enterTransition = MaterialContainerTransform().apply {
                // Manually add the Views to be shared since this is not a standard Fragment to Fragment
                // shared element transition.
                startView = requireActivity().findViewById(R.id.fab)
                endView = constraintLayout
                duration = resources.getInteger(R.integer.motion_default_large).toLong()
                interpolator = context.themeInterpolator(R.attr.motionInterpolatorPersistent)
            }
            returnTransition = Slide().apply {
                duration = resources.getInteger(R.integer.motion_duration_medium).toLong()
                interpolator = context.themeInterpolator(R.attr.motionInterpolatorOutgoing)
            }
        }

        startPostponedEnterTransition()
    }

    companion object {
        const val PAGE_AUTHOR_NAME = "PAGE_AUTHOR_NAME"
        const val PAGE_AUTHOR_URL = "PAGE_AUTHOR_URL"
        const val PAGE_ITEMS = "PAGE_ITEMS"

        private const val REQUEST_INSERT_IMAGE = 101
    }
}