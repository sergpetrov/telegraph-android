package com.telex.base.presentation.page.adapter

import android.app.Activity
import android.text.Editable
import android.text.TextWatcher
import android.view.KeyEvent
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import com.telex.base.analytics.AnalyticsHelper
import com.telex.base.extention.hideKeyboard
import com.telex.base.presentation.page.format.Format
import com.telex.base.presentation.page.format.FormatType
import com.telex.base.presentation.page.format.ImageFormat
import com.telex.base.presentation.page.options.blocks.ImageBlockMoreOptionsFragment
import kotlinx.android.synthetic.main.item_format_image.view.*
import kotlinx.android.synthetic.main.layout_item_format_more.view.*

/**
 * @author Sergey Petrov
 */
class FormatImageViewHolder(
    parent: ViewGroup,
    private val adapter: FormatAdapter
) : BaseFormatViewHolder<ImageFormat>(FormatImageItemView(parent.context)) {

    override fun bind(item: Format) {
        super.bind(item)

        (itemView as FormatImageItemView).bind(format)

        with(itemView) {

            captionEditText.onFocusChangeListener = View.OnFocusChangeListener { _, hasFocus ->
                if (hasFocus) {
                    adapter.focusedItem = item
                    captionEditText.setSelection(captionEditText.text.length)
                } else {
                    adapter.focusedItem = null
                }
            }

            captionEditText.removeTextChangedListener(textWatcher)
            captionEditText.addTextChangedListener(textWatcher)

            captionEditText.setOnKeyListener { _, _, event ->
                if (event.action == KeyEvent.ACTION_DOWN && event.keyCode == KeyEvent.KEYCODE_DEL) {
                    if (captionEditText.selectionStart == 0) {
                        adapter.removeBlockFormatItemByBackSpace(format)
                    }
                    true
                } else false
            }

            imageView.setOnClickListener {
                requestViewHolderFocus()
                showBlockMoreOptions(adapter, format)
            }

            blockMoreLayout.setOnClickListener {
                requestViewHolderFocus()
                showBlockMoreOptions(adapter, format)
            }

            blockMoreLayout.setOnLongClickListener {
                (context as Activity).hideKeyboard()
                adapter.itemTouchHelper?.startDrag(this@FormatImageViewHolder)
                adapter.focusedItem = format
                true
            }
        }
    }

    override fun unbind() {
        (itemView as FormatImageItemView).unbind()
    }

    override fun requestViewHolderFocus() {
        super.requestViewHolderFocus()
        itemView.captionEditText.requestFocus()
    }

    override fun showBlockMoreOptions(adapter: FormatAdapter, format: ImageFormat) {
        ImageBlockMoreOptionsFragment.newInstance(format.getFullUrl()).apply {
            deleteOption.onClick = {
                AnalyticsHelper.logDeleteBlock()
                adapter.removeBlockFormatItem(adapterPosition)
            }
            duplicateOption.onClick = {
                AnalyticsHelper.logDuplicateBlock()
                adapter.duplicateBlockFormatItem(adapterPosition)
            }
            show((itemView.context as AppCompatActivity).supportFragmentManager)
        }
    }

    private val textWatcher = object : TextWatcher {
        override fun afterTextChanged(s: Editable?) {
            format.caption = itemView.captionEditText.text.toString()
            adapter.onItemChanged.invoke(format)
        }

        override fun beforeTextChanged(source: CharSequence?, start: Int, count: Int, after: Int) {
        }

        override fun onTextChanged(source: CharSequence?, start: Int, before: Int, count: Int) {
            val s = source.toString()
            if (s.contains("\n")) {
                val text = s.replace("\n".toRegex(), "")
                itemView.captionEditText.setText(text)
                adapter.addBlockFormatItem(adapterPosition + 1, Format(FormatType.PARAGRAPH))
            }
        }
    }
}
