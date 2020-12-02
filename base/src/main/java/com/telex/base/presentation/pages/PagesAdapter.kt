package com.telex.base.presentation.pages

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.telex.base.R
import com.telex.base.extention.isUrl
import com.telex.base.extention.loadImage
import com.telex.base.model.source.local.entity.Page
import com.telex.base.model.system.ServerManager
import com.telex.base.presentation.page.EditorMode
import com.telex.base.presentation.page.options.PageOptionsFragment
import kotlinx.android.synthetic.main.item_page.view.*

/**
 * @author Sergey Petrov
 */
class PagesAdapter(
        private val context: Context,
        val onItemClickListener: (View, Page, EditorMode) -> Unit,
        private var items: ArrayList<Page> = ArrayList(),
        private val onNextPageListener: (() -> Unit)?
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var footer: Footer? = null
    private var hasMore = false

    fun setData(pages: List<Page>, hasMore: Boolean) {
        this.hasMore = hasMore

        val oldItems = items.toList()
        items.clear()
        items.addAll(pages)

        DiffUtil
                .calculateDiff(DiffCallback(oldItems, items), false)
                .dispatchUpdatesTo(this)

        if (hasMore && items.isEmpty()) {
            onNextPageListener?.invoke()
        }
    }

    override fun getItemViewType(position: Int): Int {
        return when {
            position >= items.size -> TYPE_FOOTER
            else -> TYPE_ITEM
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            TYPE_ITEM -> ItemViewHolder(context, LayoutInflater.from(context).inflate(R.layout.item_page, parent, false))
            TYPE_FOOTER -> {
                val footer = footer ?: throw IllegalArgumentException("footer can't be null")
                footer.itemView?.let { itemView ->
                    FooterViewHolder(itemView)
                } ?: footer.layoutId?.let { layoutId ->

                    FooterViewHolder(LayoutInflater.from(context).inflate(layoutId, parent, false))
                } ?: throw IllegalArgumentException("itemView or layoutId for footer can't be null")
            }
            else -> throw IllegalArgumentException("Unknown viewType")
        }
    }

    override fun onBindViewHolder(h: RecyclerView.ViewHolder, position: Int) {
        when (getItemViewType(position)) {
            TYPE_ITEM -> {
                val holder = h as ItemViewHolder
                val page = items[position]
                holder.bind(page)

                val cardView = holder.itemView.cardView
                cardView.transitionName = context.getString(R.string.page_item_view_shared_element, page.id.toString())

                holder.itemView.setOnClickListener { onItemClickListener.invoke(cardView, page, EditorMode.Edit) }

                holder.itemView.setOnLongClickListener {
                    PageOptionsFragment.newInstance(
                            mode = EditorMode.View,
                            pageId = page.id,
                            pagePath = page.path,
                            draft = page.draft
                    ).apply {
                        editOption.onClick = { onItemClickListener.invoke(cardView, page, EditorMode.Edit) }
                    }.also {
                        it.show((context as AppCompatActivity).supportFragmentManager)
                    }

                    true
                }

                if (position == items.size - NEXT_PAGE_THRESHOLD || (position == items.size - 1 && hasMore)) {
                    onNextPageListener?.invoke()
                }
            }
        }
    }

    override fun getItemCount(): Int {
        return when {
            footer != null -> items.size + 1
            else -> items.size
        }
    }

    fun showProgress() {
        addFooter(Footer(R.layout.item_footer_progress))
    }

    fun hideProgress() {
        removeFooter()
    }

    fun addFooter(footer: Footer) {
        this.footer = footer
        notifyItemInserted(itemCount - 1)
    }

    fun removeFooter() {
        if (footer != null) {
            notifyItemRemoved(itemCount - 1)
            footer = null
        }
    }

    class ItemViewHolder(var context: Context, itemView: View) : RecyclerView.ViewHolder(itemView) {

        fun bind(page: Page) {
            val imageUrl = page.imageUrl
            with(itemView) {
                if (page.authorName.isNullOrEmpty()) {
                    authorTextView.visibility = View.GONE
                } else {
                    authorTextView.visibility = View.VISIBLE
                    authorTextView.text = page.authorName
                }
                if (!page.title.isNullOrBlank()) {
                    titleTextView.text = page.title
                } else {
                    titleTextView.text = context.getString(R.string.untitled)
                }
                viewsTextView.text = page.views.toString()
                if (imageUrl == null || imageUrl.isEmpty()) {
                    pageImageView.visibility = View.GONE
                } else {
                    pageImageView.visibility = View.VISIBLE
                    val size = context.resources.getDimension(R.dimen.height_item_page).toInt()
                    pageImageView.loadImage(
                            context = context,
                            url = if (!imageUrl.isUrl()) ServerManager.endPoint + imageUrl else imageUrl,
                            width = size,
                            height = size
                    )
                }
            }
        }
    }

    class FooterViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

    private class DiffCallback(private val oldItems: List<Page>, private val newItems: List<Page>) : DiffUtil.Callback() {
        override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            return oldItems[oldItemPosition].id == newItems[newItemPosition].id
        }

        override fun getOldListSize(): Int {
            return oldItems.size
        }

        override fun getNewListSize(): Int {
            return newItems.size
        }

        override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            return oldItems[oldItemPosition] == newItems[newItemPosition]
        }
    }

    class Footer {
        var layoutId: Int? = null
        var itemView: View? = null

        constructor(layoutId: Int) {
            this.layoutId = layoutId
        }

        constructor(itemView: View) {
            this.itemView = itemView
        }
    }

    companion object {
        private const val NEXT_PAGE_THRESHOLD = 10
        private const val TYPE_ITEM = 0
        private const val TYPE_FOOTER = 1
    }
}
