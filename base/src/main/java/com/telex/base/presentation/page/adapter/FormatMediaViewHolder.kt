package com.telex.base.presentation.page.adapter

import android.annotation.SuppressLint
import android.app.Activity
import android.graphics.Color
import android.text.Editable
import android.text.TextWatcher
import android.view.KeyEvent
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.webkit.WebSettings
import android.webkit.WebView
import androidx.appcompat.app.AppCompatActivity
import com.telex.base.R
import com.telex.base.analytics.AnalyticsHelper
import com.telex.base.extention.hideKeyboard
import com.telex.base.model.system.ServerManager
import com.telex.base.presentation.page.format.Format
import com.telex.base.presentation.page.format.FormatType
import com.telex.base.presentation.page.format.MediaFormat
import com.telex.base.presentation.page.format.VideoFormat
import com.telex.base.presentation.page.options.blocks.MediaBlockMoreOptionsFragment
import kotlinx.android.synthetic.main.item_format_media.view.*
import kotlinx.android.synthetic.main.layout_item_format_more.view.*

/**
 * @author Sergey Petrov
 */
class FormatMediaViewHolder(
    parent: ViewGroup,
    private val adapter: FormatAdapter
) : BaseFormatViewHolder<MediaFormat>(parent, R.layout.item_format_media) {

    override fun bind(item: Format) {
        super.bind(item)

        with(itemView) {
            setOnClickListener { requestViewHolderFocus() }

            configureWebView(webView)

            val webViewLayoutParams = webView.layoutParams
            if (format is VideoFormat || format.src.contains("youtube") || format.src.contains("vimeo")) {
                webViewLayoutParams.height = context.resources.getDimension(R.dimen.web_view_video_height).toInt()
            } else {
                webViewLayoutParams.height = MATCH_PARENT
            }

            webView.loadUrl(ServerManager.endPoint + format.src)

            captionEditText.setText(format.caption)

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

            blockMoreLayout.setOnClickListener {
                requestViewHolderFocus()
                showBlockMoreOptions(adapter, format)
            }

            blockMoreLayout.setOnLongClickListener {
                (context as Activity).hideKeyboard()
                adapter.itemTouchHelper?.startDrag(this@FormatMediaViewHolder)
                adapter.focusedItem = format
                true
            }
        }
    }

    override fun requestViewHolderFocus() {
        super.requestViewHolderFocus()
        itemView.captionEditText.requestFocus()
    }

    override fun showBlockMoreOptions(adapter: FormatAdapter, format: MediaFormat) {
        MediaBlockMoreOptionsFragment.newInstance(format.getUrl()).apply {
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

    @SuppressLint("SetJavaScriptEnabled")
    private fun configureWebView(webView: WebView) {
        webView.setBackgroundColor(Color.TRANSPARENT)
        webView.isVerticalScrollBarEnabled = false
        webView.isHorizontalScrollBarEnabled = false

        with(webView.settings) {
            javaScriptEnabled = true
            cacheMode = WebSettings.LOAD_CACHE_ELSE_NETWORK
            setAppCacheEnabled(true)
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
