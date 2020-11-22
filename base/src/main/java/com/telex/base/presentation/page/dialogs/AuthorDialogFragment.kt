package com.telex.base.presentation.page.dialogs

import android.app.Dialog
import android.view.View
import android.view.WindowManager
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.telex.base.R
import com.telex.base.extention.color
import com.telex.base.extention.isUrl
import com.telex.base.extention.toNetworkUrl
import com.telex.base.presentation.base.BaseBottomSheetFragment
import com.telex.base.utils.CharacterCountErrorWatcher
import com.telex.base.utils.Constants.AUTHOR_NAME_LIMIT
import com.telex.base.utils.Constants.AUTHOR_URL_LIMIT
import kotlinx.android.synthetic.main.dialog_author.*
import moxy.presenter.InjectPresenter
import moxy.presenter.ProvidePresenter

/**
 * @author Sergey Petrov
 */
class AuthorDialogFragment : BaseBottomSheetFragment(), AuthorView {
    var onAddClickListener: ((String, String) -> Unit)? = null
    private var authorName: String? = null
    private var authorUrl: String? = null

    @InjectPresenter
    lateinit var presenter: AuthorDialogPresenter

    @ProvidePresenter
    fun providePresenter(): AuthorDialogPresenter {
        return scope.getInstance(AuthorDialogPresenter::class.java)
    }

    override val layout: Int
        get() = R.layout.dialog_author

    override fun setupView(dialog: Dialog) {
        dialog.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
        dialog.setOnShowListener {
            dialog.authorNameEditText.requestFocus()
            (dialog as BottomSheetDialog).behavior.state = BottomSheetBehavior.STATE_EXPANDED

            if (authorName.isNullOrBlank() && authorUrl.isNullOrBlank()) {
                presenter.loadUser()
            } else {
                showAuthor(authorName, authorUrl)
            }
        }
        dialog.submitButton.setOnClickListener {
            if (isAuthorInputsValid()) {
                val url = dialog.authorUrlEditText.text.toString()
                onAddClickListener?.invoke(dialog.authorNameEditText.text.toString(), url.toNetworkUrl())
                dismiss()
            }
        }
    }

    override fun showAuthor(name: String?, url: String?) {
        dialog?.let {
            it.authorNameEditText.setText(name)
            it.authorUrlEditText.setText(url)
        }
    }

    private fun isAuthorInputsValid(): Boolean {
        var isValid = true
        dialog?.let { dialog ->
            val authorNameLength = dialog.authorNameEditText.text.toString().trim().length
            if (authorNameLength !in AUTHOR_NAME_LIMIT) {
                isValid = false
                dialog.authorNameEditText.addTextChangedListener(CharacterCountErrorWatcher(dialog.authorNameEditText, dialog.authorNameLimitTextView, AUTHOR_NAME_LIMIT))
            }
            val authorUrl = dialog.authorUrlEditText.text.toString().trim()
            if (authorUrl.length !in AUTHOR_URL_LIMIT) {
                isValid = false
                dialog.authorUrlEditText.addTextChangedListener(CharacterCountErrorWatcher(dialog.authorUrlEditText, dialog.authorUrlErrorTextView, AUTHOR_URL_LIMIT))
            }

            if (authorUrl.isNotEmpty() && !authorUrl.isUrl()) {
                isValid = false
                dialog.authorUrlErrorTextView.text = getString(R.string.url_invalid)
                dialog.authorUrlErrorTextView.visibility = View.VISIBLE
                dialog.authorUrlErrorTextView.setTextColor(resources.color(R.color.error))
            }
        }
        return isValid
    }

    companion object {

        fun newInstance(
            authorName: String?,
            authorUrl: String?,
            onAddClickListener: (String, String) -> Unit
        ): AuthorDialogFragment {
            val fragment = AuthorDialogFragment()
            fragment.authorName = authorName
            fragment.authorUrl = authorUrl
            fragment.onAddClickListener = onAddClickListener
            return fragment
        }
    }
}
