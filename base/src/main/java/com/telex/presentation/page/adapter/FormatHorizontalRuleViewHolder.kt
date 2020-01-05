package com.telex.presentation.page.adapter

import android.app.Activity
import android.view.KeyEvent
import android.view.ViewGroup
import com.telex.R
import com.telex.extention.hideKeyboard
import com.telex.presentation.page.format.Format
import com.telex.presentation.page.format.FormatType.PARAGRAPH
import kotlinx.android.synthetic.main.layout_item_format_more.view.*

/**
 * @author Sergey Petrov
 */
class FormatHorizontalRuleViewHolder(
    parent: ViewGroup,
    private val adapter: FormatAdapter
) : BaseFormatViewHolder<Format>(parent, R.layout.item_format_horizontal_rule) {

    override fun bind(item: Format) {
        format = item

        with(itemView) {
            setOnFocusChangeListener { _, hasFocus ->
                if (hasFocus) {
                    adapter.focusedItem = item
                    background = context.getDrawable(R.drawable.background_hr)
                } else {
                    adapter.focusedItem = null
                    background = null
                }
            }
            setOnKeyListener { _, _, event ->
                if (event.action == KeyEvent.ACTION_DOWN) {
                    when (event.keyCode) {
                        KeyEvent.KEYCODE_ENTER -> adapter.addBlockFormatItem(adapterPosition + 1, Format(PARAGRAPH))
                        KeyEvent.KEYCODE_DEL -> adapter.removeBlockFormatItemByBackSpace(format)
                    }
                }
                false
            }
            blockMoreLayout.setOnClickListener {
                requestFocus()
                showBlockMoreOptions(adapter, item)
            }

            blockMoreLayout.setOnLongClickListener {
                requestFocus()
                (context as Activity).hideKeyboard()
                adapter.itemTouchHelper?.startDrag(this@FormatHorizontalRuleViewHolder)
                true
            }
        }
    }

    override fun requestViewHolderFocus() {
        super.requestViewHolderFocus()
        itemView.requestFocus()
    }
}
