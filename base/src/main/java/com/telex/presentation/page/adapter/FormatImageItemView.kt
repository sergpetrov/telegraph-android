package com.telex.presentation.page.adapter

import android.content.Context
import android.util.AttributeSet
import android.view.ViewGroup
import android.widget.FrameLayout
import com.telex.R
import com.telex.di.Scopes
import com.telex.extention.loadImage
import com.telex.extention.setGone
import com.telex.presentation.base.BaseFrameLayout
import com.telex.presentation.page.format.ImageFormat
import kotlinx.android.synthetic.main.item_format_image.view.*
import moxy.presenter.InjectPresenter
import moxy.presenter.ProvidePresenter
import timber.log.Timber
import toothpick.Toothpick

/**
 * @author Sergey Petrov
 */
class FormatImageItemView(
    context: Context
) : BaseFrameLayout(context), FormatImageItemMvpView {

    override val layoutRes: Int
        get() = R.layout.item_format_image

    @InjectPresenter
    lateinit var presenter: FormatImageItemPresenter

    @ProvidePresenter
    fun providePresenter(): FormatImageItemPresenter {
        return Toothpick.openScope(Scopes.App).getInstance(FormatImageItemPresenter::class.java)
    }

    override fun init(context: Context, attrs: AttributeSet?, defStyleAttr: Int, defStyleRes: Int) {
        super.init(context, attrs, defStyleAttr, defStyleRes)
        layoutParams = FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
    }

    fun bind(format: ImageFormat) {
        progressBar.setGone(false)

        if (format.url.startsWith("file:")) {
            presenter.uploadImage(format)
            showPreview(format.url)
        } else {
            showImage(format.getFullUrl())
        }

        if (format.caption.isNotEmpty()) {
            captionEditText.setText(format.caption)
        }
    }

    fun unbind() {
        previewImageView.setImageBitmap(null)
        imageView.setImageBitmap(null)
        captionEditText.text = null
    }

    override fun updateImage(format: ImageFormat) {
        showImage(format.getFullUrl())
    }

    private fun showPreview(url: String) {
        previewImageView.setGone(false)
        previewImageView.loadImage(context, url)
    }

    private fun showImage(url: String) {
        imageView.loadImage(context, url,
                onLoaded = {
                    previewImageView.setImageBitmap(null)
                    previewImageView.setGone(true)
                    progressBar.setGone(true)
                },
                onFailed = { error ->
                    Timber.e(error)
                    showError(R.string.something_went_wrong)
                }
        )
    }
}
