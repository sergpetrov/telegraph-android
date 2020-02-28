package com.telex.base.presentation.page

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View.GONE
import android.view.View.VISIBLE
import android.widget.LinearLayout
import androidx.appcompat.widget.TooltipCompat
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.telex.base.R
import com.telex.base.analytics.AnalyticsHelper
import com.telex.base.extention.disable
import com.telex.base.extention.enable
import com.telex.base.extention.getColorFromAttr
import com.telex.base.extention.setGone
import com.telex.base.extention.showKeyboard
import com.telex.base.model.source.local.entity.Page
import com.telex.base.presentation.base.BaseActivity
import com.telex.base.presentation.page.EditorMode.Edit
import com.telex.base.presentation.page.adapter.FormatAdapter
import com.telex.base.presentation.page.dialogs.AuthorDialogFragment
import com.telex.base.presentation.page.dialogs.InsertIframeDialogFragment
import com.telex.base.presentation.page.dialogs.InsertImageUrlCaptionDialogFragment
import com.telex.base.presentation.page.format.Format
import com.telex.base.presentation.page.format.FormatType
import com.telex.base.presentation.page.format.FormatType.ASIDE
import com.telex.base.presentation.page.format.FormatType.BOLD
import com.telex.base.presentation.page.format.FormatType.HEADING
import com.telex.base.presentation.page.format.FormatType.HORIZONTAL_RULE
import com.telex.base.presentation.page.format.FormatType.ITALIC
import com.telex.base.presentation.page.format.FormatType.LINK
import com.telex.base.presentation.page.format.FormatType.ORDERED_LIST
import com.telex.base.presentation.page.format.FormatType.PARAGRAPH
import com.telex.base.presentation.page.format.FormatType.QUOTE
import com.telex.base.presentation.page.format.FormatType.STRIKETHROUGH
import com.telex.base.presentation.page.format.FormatType.STRONG
import com.telex.base.presentation.page.format.FormatType.SUB_HEADING
import com.telex.base.presentation.page.format.FormatType.UNDERLINE
import com.telex.base.presentation.page.format.FormatType.UNORDERED_LIST
import com.telex.base.presentation.page.format.ImageFormat
import com.telex.base.presentation.page.options.InsertImageOptionsFragment
import com.telex.base.presentation.page.options.PageOptionsFragment
import com.telex.base.utils.PermissionsHelper
import kotlinx.android.synthetic.main.activity_page.*
import kotlinx.android.synthetic.main.layout_editor_toolbar.*
import kotlinx.android.synthetic.main.layout_editor_toolbar.view.*
import moxy.presenter.InjectPresenter
import moxy.presenter.ProvidePresenter

/**
 * @author Sergey Petrov
 */
class PageActivity : BaseActivity(), PageView {

    override val layoutRes = R.layout.activity_page

    private val formatAdapter: FormatAdapter by lazy {
        FormatAdapter(
                onFocusItemChanged = { format -> onFocusItemChanged(format) },
                onTextSelected = this::onTextSelected,
                onItemChanged = { onDraftChanged() },
                onPaste = { html -> presenter.convertHtml(html) }
        )
    }

    private var formatAdapterDataObserver: RecyclerView.AdapterDataObserver? = null
    private val addImageFromStorageDelegate by lazy { AddImageFromStorageDelegate(this) }

    private var pageId: Long? = null
    private lateinit var mode: EditorMode
    private var authorName: String? = null
    private var authorUrl: String? = null
    private var title: String? = null

    @InjectPresenter
    lateinit var presenter: PagePresenter

    @ProvidePresenter
    fun providePresenter(): PagePresenter {
        return scope.getInstance(PagePresenter::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        intent?.let {
            mode = intent.getSerializableExtra(EXTRA_MODE) as EditorMode
            pageId = intent.getLongExtra(PAGE_ID, -1L)
            if (pageId == -1L) pageId = null
            authorName = intent.getStringExtra(PAGE_AUTHOR_NAME)
            authorUrl = intent.getStringExtra(PAGE_AUTHOR_URL)
            title = intent.getStringExtra(PAGE_TITLE)
        }

        with(recyclerView) {
            val linearLayoutManager = LinearLayoutManager(context)
            layoutManager = linearLayoutManager
            adapter = formatAdapter
            setItemViewCacheSize(100)
            itemAnimator = null
            val gestureDetector = GestureDetector(context, object : GestureDetector.SimpleOnGestureListener() {
                override fun onSingleTapUp(e: MotionEvent?): Boolean {
                    val lastItem = formatAdapter.items.lastOrNull()
                    if (lastItem == null || lastItem.html != Format.getEmptyHtml(lastItem.type) || lastItem.type == HORIZONTAL_RULE) {
                        formatAdapter.addBlockFormatItem(formatAdapter.items.size + 1, Format(PARAGRAPH))
                    } else {
                        formatAdapter.requestFocusForFormatItem(lastItem)
                    }
                    recyclerView?.postDelayed({ showKeyboard() }, 50)
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

        doneImageView.setOnClickListener { doneOnClicked() }
        closeImageView.setOnClickListener { finish() }

        moreImageView.setOnClickListener { presenter.onMoreClicked() }

        if (!title.isNullOrBlank()) {
            formatAdapter.pageTitle = title
        }

        presenter.openPage(pageId)

        setupEditorToolbar()
        setupMode(mode)
        setupAuthor()
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
                mode = Edit,
                pageId = page.id,
                pagePath = page.path,
                draft = page.draft
        ).apply {
            publishOption.onClick = { doneOnClicked() }
            onDraftDiscardedListener = { this@PageActivity.presenter.isDraftNeeded = false }
            onOnPostDeletedListener = { finish() }
            show(supportFragmentManager)
        }
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle?) {
        super.onRestoreInstanceState(savedInstanceState)
        savedInstanceState?.let {
            mode = intent.getSerializableExtra(EXTRA_MODE) as EditorMode
            pageId = savedInstanceState.getLong(PAGE_ID)
            authorName = savedInstanceState.getString(PAGE_AUTHOR_NAME)
            authorUrl = savedInstanceState.getString(PAGE_AUTHOR_URL)
            title = savedInstanceState.getString(PAGE_TITLE)
            recyclerView.layoutManager?.onRestoreInstanceState(savedInstanceState.getParcelable(PAGE_ITEMS))
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putSerializable(EXTRA_MODE, mode.name)
        pageId?.let { outState.putLong(PAGE_ID, it) }
        outState.putString(PAGE_AUTHOR_NAME, authorName)
        outState.putString(PAGE_AUTHOR_URL, authorUrl)
        outState.putString(PAGE_TITLE, title)
        outState.putParcelable(PAGE_ITEMS, recyclerView.layoutManager?.onSaveInstanceState())
    }

    override fun onDestroy() {
        super.onDestroy()
        presenter.discardDraftPageIfNeeded(getPageTitle(), formatAdapter.items)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (RESULT_OK == resultCode && data != null) {

            when (requestCode) {
                REQUEST_INSERT_IMAGE -> {
                    val images = addImageFromStorageDelegate.convertIntentDataToImageFormats(baseContext, data)

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
        finish()
    }

    override fun showPage(page: Page, formats: List<Format>) {
        with(formatAdapter) {
            pageTitle = page.title
            items = ArrayList(formats)
            notifyDataSetChanged()
            focusedItem = null
        }

        if (formatAdapterDataObserver != null) {
            formatAdapter.unregisterAdapterDataObserver(requireNotNull(formatAdapterDataObserver))
        } else {
            formatAdapterDataObserver = createFormatAdapterDataObserver()
        }
        formatAdapter.registerAdapterDataObserver(requireNotNull(formatAdapterDataObserver))
    }

    private fun setupEditorToolbar() {
        with(editorToolbar) {
            boldFormatButton.setOnClickListener { formatAdapter.toggleFormat(STRONG) }
            boldFormatButton.tooltipText = getString(R.string.format_bold)

            italicFormatButton.setOnClickListener { formatAdapter.toggleFormat(ITALIC) }
            italicFormatButton.tooltipText = getString(R.string.format_italic)

            underlineFormatButton.setOnClickListener { formatAdapter.toggleFormat(UNDERLINE) }
            underlineFormatButton.tooltipText = getString(R.string.format_underline)

            strikethroughFormatButton.setOnClickListener { formatAdapter.toggleFormat(STRIKETHROUGH) }
            strikethroughFormatButton.tooltipText = getString(R.string.format_strike)

            linkButton.setOnClickListener {
                linkButton.isChecked = !linkButton.isChecked
                formatAdapter.toggleFormat(LINK)
            }
            linkButton.tooltipText = getString(R.string.format_link)

            paragraphButton.setOnClickListener { formatAdapter.toggleFormat(PARAGRAPH) }
            paragraphButton.tooltipText = getString(R.string.format_paragraph)

            quoteFormatButton.setOnClickListener { formatAdapter.toggleFormat(QUOTE) }
            quoteFormatButton.tooltipText = getString(R.string.format_block_quote)

            insertLineButton.setOnClickListener { formatAdapter.addBlockFormatItem(Format(HORIZONTAL_RULE)) }
            TooltipCompat.setTooltipText(insertLineButton, getString(R.string.format_hr))

            headingFormatButton.setOnClickListener { formatAdapter.toggleFormat(HEADING) }
            headingFormatButton.tooltipText = getString(R.string.format_heading)

            subHeadingFormatButton.setOnClickListener { formatAdapter.toggleFormat(SUB_HEADING) }
            subHeadingFormatButton.tooltipText = getString(R.string.format_subheading)

            unorderedListFormatButton.setOnClickListener { formatAdapter.toggleFormat(UNORDERED_LIST) }
            unorderedListFormatButton.tooltipText = getString(R.string.format_list_unordered)

            orderedListFormatButton.setOnClickListener { formatAdapter.toggleFormat(ORDERED_LIST) }
            orderedListFormatButton.tooltipText = getString(R.string.format_list_ordered)

            insertImageButton.setOnClickListener {
                InsertImageOptionsFragment().apply {
                    fromGalleryOption.onClick = {
                        addImageFromStorageDelegate.showAlert(endAction = { chooseImage(REQUEST_INSERT_IMAGE) })
                    }
                    byUrlOption.onClick = { showImageUrlCaptionDialog(null) }
                    show(supportFragmentManager, tag)
                }
            }
            TooltipCompat.setTooltipText(insertImageButton, getString(R.string.insert_image))

            insertIframeButton.setOnClickListener {
                InsertIframeDialogFragment().apply {
                    onAddClickListener = { format -> addBlockFormatItem(format) }
                    show(supportFragmentManager, tag)
                }
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
            val builder = MaterialAlertDialogBuilder(this)
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
                finish()
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
        if (mode == Edit) {
            moreImageView.visibility = VISIBLE
        } else {
            (doneImageView.layoutParams as LinearLayout.LayoutParams).setMargins(0, 0, resources.getDimensionPixelSize(R.dimen.margin_8), 0)
            moreImageView.visibility = GONE
        }
    }

    private fun setupAuthor() {
        setupAuthorName(authorName)
        authorLayout.setOnClickListener {
            AuthorDialogFragment.newInstance(
                    authorName, authorUrl,
                    onAddClickListener = { authorName, authorUrl ->
                        this.authorName = authorName
                        this.authorUrl = authorUrl

                        setupAuthorName(authorName)
                    }
            ).apply { show(supportFragmentManager, tag) }
        }
    }

    private fun setupAuthorName(authorName: String?) {
        if (authorName.isNullOrBlank()) {
            authorNameTextView.text = getString(R.string.add_name)
        } else {
            authorNameTextView.text = authorName
            authorNameTextView.setTextColor(getColorFromAttr(R.attr.colorAccent))
        }
    }

    private fun chooseImage(requestCode: Int) {
        if (PermissionsHelper.checkPermissions(this, requestCode, Manifest.permission.READ_EXTERNAL_STORAGE)) {
            addImageFromStorageDelegate.startActivityForResult(this, requestCode)
        }
    }

    private fun showImageUrlCaptionDialog(url: String?) {
        InsertImageUrlCaptionDialogFragment.newInstance(url,
                onAddClickListener = { imageUrl, caption -> addBlockFormatItem(ImageFormat(imageUrl, caption)) }
        ).apply { show(supportFragmentManager, tag) }
    }

    private fun addBlockFormatItem(format: Format) {
        formatAdapter.addBlockFormatItem(format)
    }

    private fun onTextSelected(selected: Boolean = false, format: Format? = null, formatTypes: List<FormatType> = emptyList()) {
        with(editorToolbar) {
            boldFormatButton.isChecked = formatTypes.contains(BOLD)
            italicFormatButton.isChecked = formatTypes.contains(ITALIC)
            strikethroughFormatButton.isChecked = formatTypes.contains(STRIKETHROUGH)
            underlineFormatButton.isChecked = formatTypes.contains(UNDERLINE)
            linkButton.isChecked = formatTypes.contains(LINK)

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
                    QUOTE, ASIDE -> italicFormatButton.disable()
                }
            }
        }
    }

    private fun onFocusItemChanged(format: Format?) {
        enableEditorToolbar(format != null || formatAdapter.items.isEmpty())

        paragraphButton.isChecked = format?.type == PARAGRAPH
        headingFormatButton.isChecked = format?.type == HEADING
        subHeadingFormatButton.isChecked = format?.type == SUB_HEADING
        quoteFormatButton.isChecked = format?.type == QUOTE
        unorderedListFormatButton.isChecked = format?.type == UNORDERED_LIST
        orderedListFormatButton.isChecked = format?.type == ORDERED_LIST
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

    companion object {
        const val PAGE_ID = "PAGE_ID"
        const val PAGE_TITLE = "PAGE_TITLE"
        const val PAGE_AUTHOR_NAME = "PAGE_AUTHOR_NAME"
        const val PAGE_AUTHOR_URL = "PAGE_AUTHOR_URL"
        const val PAGE_ITEMS = "PAGE_ITEMS"
        const val EXTRA_MODE = "EXTRA_MODE"

        private const val REQUEST_INSERT_IMAGE = 101
    }
}
