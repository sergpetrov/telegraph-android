package com.telex.base.presentation.page.adapter

import android.app.Activity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.telex.base.analytics.AnalyticsHelper
import com.telex.base.extention.showKeyboard
import com.telex.base.presentation.page.format.Format
import com.telex.base.presentation.page.format.FormatType
import com.telex.base.presentation.page.options.blocks.BlockMoreOptionsFragment

/**
 * @author Sergey Petrov
 */
abstract class BaseFormatViewHolder<T : Format> : RecyclerView.ViewHolder {

    internal lateinit var format: T

    constructor(parent: ViewGroup, layout: Int) : super(LayoutInflater.from(parent.context).inflate(layout, parent, false))

    constructor(itemView: View) : super(itemView)

    @Suppress("UNCHECKED_CAST")
    open fun bind(item: Format) {
        format = item as T
    }

    open fun requestViewHolderFocus() {}

    open fun clearViewHolderFocus() {
        itemView.clearFocus()
    }

    open fun toggleFormat(formatType: FormatType) {}

    open fun unbind() {}

    fun getFormat(): T = format

    fun showKeyboard() {
        itemView.postDelayed({ (itemView.context as Activity).showKeyboard() }, 50)
    }

    protected open fun showBlockMoreOptions(adapter: FormatAdapter, format: T) {
        BlockMoreOptionsFragment().apply {
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
}
