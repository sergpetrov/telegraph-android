package com.telex.base.presentation.page.adapter

import android.app.Activity
import android.os.Parcelable
import android.text.Editable
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.TextWatcher
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import com.telex.base.extention.hideKeyboard
import com.telex.base.extention.isUrl
import com.telex.base.model.system.ServerManager
import com.telex.base.presentation.page.dialogs.AddLinkDialogFragment
import com.telex.base.presentation.page.format.Format
import com.telex.base.presentation.page.format.FormatType
import com.telex.base.presentation.page.format.FormatType.ASIDE
import com.telex.base.presentation.page.format.FormatType.BOLD
import com.telex.base.presentation.page.format.FormatType.HEADING
import com.telex.base.presentation.page.format.FormatType.ITALIC
import com.telex.base.presentation.page.format.FormatType.LINK
import com.telex.base.presentation.page.format.FormatType.ORDERED_LIST
import com.telex.base.presentation.page.format.FormatType.PARAGRAPH
import com.telex.base.presentation.page.format.FormatType.PREFORMAT
import com.telex.base.presentation.page.format.FormatType.QUOTE
import com.telex.base.presentation.page.format.FormatType.STRIKETHROUGH
import com.telex.base.presentation.page.format.FormatType.STRONG
import com.telex.base.presentation.page.format.FormatType.SUB_HEADING
import com.telex.base.presentation.page.format.FormatType.UNDERLINE
import com.telex.base.presentation.page.format.FormatType.UNORDERED_LIST
import com.telex.base.utils.Constants
import com.telex.base.utils.Constants.END_OF_BUFFER_MARKER
import com.telex.base.utils.Constants.NEWLINE
import kotlinx.android.synthetic.main.item_format_text.view.*
import kotlinx.android.synthetic.main.layout_item_format_more.view.*
import org.wordpress.aztec.AztecText
import org.wordpress.aztec.History
import org.wordpress.aztec.toolbar.ToolbarAction
import timber.log.Timber

/**
 * @author Sergey Petrov
 */
class FormatTextViewHolder(
    layout: Int,
    parent: ViewGroup,
    private val adapter: FormatAdapter,
    private val onTextSelected: (Boolean, Format?, List<FormatType>) -> Unit,
    private val onPaste: (html: String) -> List<Format>
) : BaseFormatViewHolder<Format>(parent, layout) {

    private var textWatcher: FormatTextWatcher? = null
    private var isInitFinished = false

    override fun bind(item: Format) {
        super.bind(item)
        isInitFinished = false

        with(itemView) {
            editText.isInCalypsoMode = false
            editText.history = History(false, 0)
            editText.isHorizontalScrollBarEnabled = true
            editText.setOnCopyPasteListener(object : AztecText.OnCopyPasteListener {
                override fun onPaste(html: String) {
                    val newItems = onPaste.invoke(html).toMutableList()
                    if (isEmptyText()) {
                        adapter.replaceBlockFormatItem(item, newItems)
                    } else {
                        adapter.addBlockFormatItems(newItems)
                    }
                }
            })

            editText.setOnEnterForBlockListener(object : AztecText.OnEnterForBlockListener {
                override fun onEnterKey(isEmpty: Boolean): Boolean {
                    if (item.type == UNORDERED_LIST || item.type == ORDERED_LIST || item.type == PREFORMAT) {
                        val paragraphFormat = Format(PARAGRAPH)
                        if (isEmpty) {
                            adapter.replaceBlockFormatItem(format, listOf(paragraphFormat))
                            showKeyboard()
                        } else {
                            editText.clearFocus()
                            adapter.addBlockFormatItem(adapterPosition + 1, paragraphFormat)
                        }
                    }
                    return true
                }
            })

            editText.onFocusChangeListener = View.OnFocusChangeListener { _, hasFocus ->
                if (hasFocus) {
                    adapter.focusedItem = item
                } else {
                    adapter.focusedItem = null
                }
            }

            editText.setOnSelectionChangedListener(object : AztecText.OnSelectionChangedListener {
                override fun onSelectionChanged(selStart: Int, selEnd: Int) {
                    if (item.type != PREFORMAT) {
                        onTextSelected(selStart, selEnd)
                    }
                }
            })

            if (textWatcher == null) {
                textWatcher = FormatTextWatcher()
                editText.addTextChangedListener(textWatcher)
            }

            editText.setAztecKeyListener(object : AztecText.OnAztecKeyListener {
                override fun onEnterKey(text: Spannable, firedAfterTextChanged: Boolean, selStart: Int, selEnd: Int): Boolean {
                    return false
                }

                override fun onBackspaceKey(): Boolean {
                    if (isInitFinished && editText.selectionStart == 0) {
                        adapter.removeBlockFormatItemByBackSpace(format)
                    }
                    return false
                }
            })

            blockMoreLayout.setOnClickListener {
                requestViewHolderFocus()
                showBlockMoreOptions(adapter, format)
            }

            blockMoreLayout.setOnLongClickListener {
                (context as Activity).hideKeyboard()
                adapter.itemTouchHelper?.startDrag(this@FormatTextViewHolder)
                adapter.focusedItem = item
                true
            }

            val editTextState = editTextStates[format.id]
            if (editTextState != null) {
                // it's needed to restore cursor position after toggle block style
                editText.onRestoreInstanceState(editTextState)
                editTextStates.remove(format.id)
            } else {
                editText.fromHtml(item.toHtml(), isInit = false)
            }
        }

        isInitFinished = true
    }

    override fun requestViewHolderFocus() {
        super.requestViewHolderFocus()
        itemView.editText.requestFocus()
    }

    override fun toggleFormat(formatType: FormatType) {
        when (formatType) {
            LINK -> showLinkDialog()
            else -> {
                toggleFormatType(formatType)
                editTextStates[format.id] = itemView.editText.onSaveInstanceState()
                updateHtml(itemView.editText.toHtml())
            }
        }
    }

    fun splitFormatItemByCursorPosition(): Format? {
        return splitFormatItem(itemView.editText.selectionStart, itemView.editText.selectionEnd)
    }

    fun isCursorPositionAtStart(): Boolean {
        return itemView.editText.selectionStart == 0
    }

    fun isCursorPositionAtEnd(): Boolean {
        return itemView.editText.selectionEnd == itemView.editText.text.length
    }

    fun isSelectionEmpty(): Boolean {
        return itemView.editText.selectionStart == itemView.editText.selectionEnd
    }

    fun getText(): CharSequence {
        return itemView.editText.text
    }

    fun isEmptyText(): Boolean {
        return itemView.editText.text.toString().isEmpty() || itemView.editText.text.toString() == Constants.END_OF_BUFFER_MARKER_STRING
    }

    fun appendText(editable: Editable) {
        with(itemView.editText) {
            text.append(editable)
            post {
                val cursorPosition = if (editable.toString() != Constants.END_OF_BUFFER_MARKER_STRING) text.length - editable.length else text.length
                if (text.length >= cursorPosition) {
                    setSelection(cursorPosition)
                }
            }
        }
    }

    private fun isSplitByBreakLineAllowed(): Boolean {
        return format.type != UNORDERED_LIST && format.type != ORDERED_LIST && format.type != PREFORMAT
    }

    private fun splitFormatItem(startPosition: Int, endPosition: Int): Format? {
        try {
            val aztecFormat = format.type.aztecFormat
            val editable = itemView.editText.text
            var tailFormatItem: Format? = null
            if (isSelectionEmpty() || endPosition < editable.length) {
                tailFormatItem = Format(
                        format.type,
                        html = cutHtmlFromSelectionPosition(
                                editable = SpannableStringBuilder(editable),
                                selectionPosition = endPosition
                        )
                )
            }

            if (aztecFormat != null) {
                itemView.editText.blockFormatter.removeBlockStyle(aztecFormat, 0, editable.length)
            }
            editable.delete(startPosition, editable.length)
            val endOfBufferMarkerIndex = editable.indexOf(END_OF_BUFFER_MARKER)
            if (endOfBufferMarkerIndex != -1 && editable[endOfBufferMarkerIndex - 1] == NEWLINE) {
                editable.delete(endOfBufferMarkerIndex - 1, endOfBufferMarkerIndex)
            }
            if (aztecFormat != null) {
                itemView.editText.blockFormatter.applyBlockStyle(aztecFormat, 0, editable.length)
            }
            updateHtml(itemView.editText.toHtml())

            return tailFormatItem
        } catch (e: Exception) {
            val error = IllegalStateException("Error on splitFormatItem for formatType=${format.type} and html=${format.toHtml()}", e)
            Timber.e(error)
        }
        return null
    }

    private fun cutHtmlFromSelectionPosition(editable: Editable, selectionPosition: Int): String {
        val aztecFormat = format.type.aztecFormat
        val editor = AztecText(itemView.context)
        editor.isInCalypsoMode = false
        editor.history = History(false, 0)
        editor.text = editable
        if (aztecFormat != null) {
            editor.blockFormatter.removeBlockStyle(aztecFormat, 0, editor.text.length)
        }
        if (selectionPosition <= editor.text.length) {
            editor.text.delete(0, selectionPosition)
        }
        if (editor.text.firstOrNull() == NEWLINE) {
            editor.text.delete(0, 1)
        }
        if (aztecFormat != null) {
            editor.blockFormatter.applyBlockStyle(aztecFormat, 0, editor.text.length)
        }
        return editor.toHtml()
    }

    private fun onTextSelected(selStart: Int, selEnd: Int) {
        with(itemView.editText) {
            val appliedStyles = getAppliedStyles(selStart, selEnd).mapNotNull { FormatType.getByAztecFormat(it) }
            postDelayed({ onTextSelected.invoke(getSelectedText().isNotEmpty(), format, appliedStyles) }, 100)
        }
    }

    private fun toggleFormatType(formatType: FormatType) {
        val blockFormatter = itemView.editText.blockFormatter
        val inlineFormatter = itemView.editText.inlineFormatter
        val textFormat = requireNotNull(formatType.aztecFormat)

        when (formatType) {
            HEADING,
            SUB_HEADING,
            PARAGRAPH,
            PREFORMAT,
            QUOTE,
            UNORDERED_LIST,
            ORDERED_LIST -> {
                format.type.aztecFormat?.let { blockFormatter.removeBlockStyle(it, 0, itemView.editText.text.length) }
                if (format.type == formatType) {
                    blockFormatter.applyBlockStyle(requireNotNull(PARAGRAPH.aztecFormat))
                    format.type = PARAGRAPH
                } else {
                    blockFormatter.applyBlockStyle(textFormat, 0, itemView.editText.text.length)
                    format.type = formatType
                }
                adapter.focusedItem = format
            }
            ITALIC,
            UNDERLINE,
            STRIKETHROUGH -> inlineFormatter.toggle(textFormat)
            BOLD,
            STRONG -> inlineFormatter.toggleAny(ToolbarAction.BOLD.textFormats)
        }
    }

    private fun showLinkDialog() {
        with(itemView.editText) {
            val urlAndAnchor = linkFormatter.getSelectedUrlWithAnchor(ignoreUrlFromClipboard = true)
            val initialUrl = urlAndAnchor.first
            val anchor = urlAndAnchor.second

            AddLinkDialogFragment.newInstance(
                    initialUrl = if (initialUrl.isNotEmpty() && !initialUrl.isUrl()) ServerManager.endPoint + initialUrl else initialUrl,
                    showRemoveButton = linkFormatter.isUrlSelected(),
                    onAddClickListener = { url ->
                        link(url, anchor, true)
                        updateHtml(toHtml())
                        onTextSelected(selectionStart, selectionEnd)
                    },
                    onRemoveClickListener = {
                        removeLink()
                        updateHtml(toHtml())
                    }
            ).apply { show((itemView.context as AppCompatActivity).supportFragmentManager, tag) }
        }
    }

    private fun updateHtml(html: String) {
        format.putHtml(html)
        if (isInitFinished) {
            adapter.onItemChanged.invoke(format)
        }
    }

    private inner class FormatTextWatcher : TextWatcher {

        override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
            if (isSplitByBreakLineAllowed()) {
                itemView.editText.disableInlineTextHandling()
                itemView.editText.disableTextChangedListener()
            }
        }

        override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
            if (before == 0 && count == 1 && s[start] == NEWLINE) {
                when (format.type) {
                    HEADING, SUB_HEADING -> splitByEnter(start)
                    PARAGRAPH, QUOTE, ASIDE -> {
                        if (start > 0 && s[start - 1] == NEWLINE) {
                            splitByEnter(start)
                        }
                    }
                }
            }
        }

        override fun afterTextChanged(s: Editable?) {
            if (isInitFinished) {
                val html = itemView.editText.toHtml()
                if (html.isNotEmpty()) {
                    updateHtml(html)
                }
            }

            itemView.editText.enableInlineTextHandling()
            itemView.editText.enableTextChangedListener()
        }

        private fun splitByEnter(cursorPosition: Int) {
            val tailFormatItem = splitFormatItem(cursorPosition, cursorPosition + 1)
            if (tailFormatItem != null) {
                adapter.addBlockFormatItem(adapterPosition + 1, tailFormatItem, ignoreSplit = true)
            }
        }
    }

    companion object {
        private val editTextStates = mutableMapOf<String, Parcelable?>()
    }
}
