package com.telex.presentation.settings

import android.os.Bundle
import android.view.View
import com.telex.R
import com.telex.extention.color
import com.telex.extention.isUrl
import com.telex.model.source.local.entity.User
import com.telex.presentation.base.BaseActivity
import com.telex.utils.CharacterCountErrorWatcher
import com.telex.utils.Constants.ACCOUNT_NAME_LIMIT
import com.telex.utils.Constants.AUTHOR_NAME_LIMIT
import com.telex.utils.Constants.AUTHOR_URL_LIMIT
import kotlinx.android.synthetic.main.activity_account_settings.*
import moxy.presenter.InjectPresenter
import moxy.presenter.ProvidePresenter

/**
 * @author Sergey Petrov
 */
class AccountSettingsActivity : BaseActivity(), AccountSettingsView {
    override val layoutRes: Int = R.layout.activity_account_settings

    @InjectPresenter
    lateinit var presenter: AccountSettingsPresenter

    @ProvidePresenter
    fun providePresenter(): AccountSettingsPresenter {
        return scope.getInstance(AccountSettingsPresenter::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setupStatusBar()

        doneImageView.setOnClickListener {
            if (isInputsValid()) {
                presenter.saveUser(
                        shortName = accountNameEditText.text.toString(),
                        authorName = authorNameEditText.text.toString(),
                        authorUrl = authorUrlEditText.text.toString()
                )
            }
        }

        closeImageView.setOnClickListener { finish() }

        moreImageView.setOnClickListener {
            AccountSettingsOptionsFragment().apply {
                logoutOption.onClick = { presenter.logout() }
                show(supportFragmentManager)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        router.setup(supportFragmentManager)
    }

    override fun onPause() {
        super.onPause()
        router.clear()
    }

    private fun isInputsValid(): Boolean {
        var isValid = true
        val accountNameLength = accountNameEditText.text.trim().length
        if (accountNameLength !in ACCOUNT_NAME_LIMIT) {
            isValid = false
            accountNameEditText.addTextChangedListener(CharacterCountErrorWatcher(accountNameEditText, accountNameLimitTextView, ACCOUNT_NAME_LIMIT))
        }
        val authorNameLength = authorNameEditText.text.trim().length
        if (authorNameLength !in AUTHOR_NAME_LIMIT) {
            isValid = false
            authorNameEditText.addTextChangedListener(CharacterCountErrorWatcher(authorNameEditText, authorNameLimitTextView, AUTHOR_NAME_LIMIT))
        }

        val authorUrl = authorUrlEditText.text.trim().toString()
        when {
            !authorUrl.isEmpty() && !authorUrl.isUrl() -> {
                isValid = false
                authorUrlErrorTextView.text = getString(R.string.url_invalid)
                authorUrlErrorTextView.visibility = View.VISIBLE
                authorUrlErrorTextView.setTextColor(resources.color(R.color.error))
            }
            authorUrl.length !in AUTHOR_URL_LIMIT -> {
                isValid = false
                authorUrlEditText.addTextChangedListener(CharacterCountErrorWatcher(authorUrlEditText, authorUrlErrorTextView, AUTHOR_URL_LIMIT))
            }
        }

        return isValid
    }

    override fun showProgress(isVisible: Boolean) {
        if (isVisible) {
            showOverlay()
        } else {
            hideOverlay()
        }
    }

    override fun showUser(user: User?) {
        user?.let {
            accountNameEditText.setText(user.accountName)
            authorNameEditText.setText(user.authorName)
            authorUrlEditText.setText(user.authorUrl)
        }
    }

    override fun onUserSaved() {
        finish()
    }
}
