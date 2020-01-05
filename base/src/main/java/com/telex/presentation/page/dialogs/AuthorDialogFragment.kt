package com.telex.presentation.page.dialogs

import android.os.Bundle
import android.view.View
import android.view.WindowManager
import androidx.appcompat.app.AlertDialog
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.telex.R
import com.telex.di.Scopes
import com.telex.extention.color
import com.telex.extention.isUrl
import com.telex.extention.toNetworkUrl
import com.telex.presentation.base.BaseDialogFragment
import com.telex.utils.CharacterCountErrorWatcher
import com.telex.utils.Constants.AUTHOR_NAME_LIMIT
import com.telex.utils.Constants.AUTHOR_URL_LIMIT
import kotlinx.android.synthetic.main.dialog_author.*
import moxy.presenter.InjectPresenter
import moxy.presenter.ProvidePresenter
import toothpick.Scope
import toothpick.Toothpick

/**
 * @author Sergey Petrov
 */
class AuthorDialogFragment : BaseDialogFragment(), AuthorView {
    var onAddClickListener: ((String, String) -> Unit)? = null
    private var authorName: String? = null
    private var authorUrl: String? = null

    @InjectPresenter
    lateinit var presenter: AuthorDialogPresenter

    @ProvidePresenter
    fun providePresenter(): AuthorDialogPresenter {
        return scope.getInstance(AuthorDialogPresenter::class.java)
    }

    private lateinit var scope: Scope

    override fun onCreate(savedInstanceState: Bundle?) {
        scope = Toothpick.openScope(Scopes.App)

        super.onCreate(savedInstanceState)
        retainInstance = true
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): AlertDialog {
        val builder = MaterialAlertDialogBuilder(context)
        builder.setView(R.layout.dialog_author)
        builder.setNegativeButton(R.string.cancel, null)
        builder.setPositiveButton(R.string.add, null)
        val dialog = builder.create()
        dialog.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE)
        dialog.setOnShowListener {
            dialog.authorNameEditText.requestFocus()

            if (authorName.isNullOrBlank() && authorUrl.isNullOrBlank()) {
                presenter.loadUser()
            } else {
                showAuthor(authorName, authorUrl)
            }
            dialog.setButton(AlertDialog.BUTTON_POSITIVE, getString(R.string.add)) { _, _ -> }
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                if (isAuthorInputsValid()) {
                    val url = dialog.authorUrlEditText.text.toString()
                    onAddClickListener?.invoke(dialog.authorNameEditText.text.toString(), url.toNetworkUrl())
                    dismiss()
                }
            }
        }
        return dialog
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

            if (!authorUrl.isEmpty() && !authorUrl.isUrl()) {
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
