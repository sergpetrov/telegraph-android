package com.telex.base.presentation.settings.account

import android.os.Bundle
import android.view.View
import androidx.navigation.fragment.findNavController
import com.telex.base.NavigationGraphDirections
import com.telex.base.R
import com.telex.base.extention.applySystemWindowInsetsPadding
import com.telex.base.extention.color
import com.telex.base.extention.isUrl
import com.telex.base.model.source.local.entity.User
import com.telex.base.presentation.base.BaseFragment
import com.telex.base.utils.CharacterCountErrorWatcher
import com.telex.base.utils.Constants
import kotlinx.android.synthetic.main.fragment_account_settings.*
import moxy.presenter.InjectPresenter
import moxy.presenter.ProvidePresenter

/**
 * @author Sergey Petrov
 */
class AccountSettingsFragment : BaseFragment(), AccountSettingsView {

    override val layoutRes: Int
        get() = R.layout.fragment_account_settings

    @InjectPresenter
    lateinit var presenter: AccountSettingsPresenter

    @ProvidePresenter
    fun providePresenter(): AccountSettingsPresenter {
        return scope.getInstance(AccountSettingsPresenter::class.java)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        rootLayout.applySystemWindowInsetsPadding(applyTop = true, applyBottom = true)

        doneImageView.setOnClickListener {
            if (isInputsValid()) {
                presenter.saveUser(
                        shortName = accountNameEditText.text.toString(),
                        authorName = authorNameEditText.text.toString(),
                        authorUrl = authorUrlEditText.text.toString()
                )
            }
        }

        closeImageView.setOnClickListener { findNavController().popBackStack() }

        moreImageView.setOnClickListener {
            AccountSettingsOptionsFragment().apply {
                logoutOption.onClick = { presenter.logout() }
            }.show(parentFragmentManager)
        }
    }

    private fun isInputsValid(): Boolean {
        var isValid = true
        val accountNameLength = accountNameEditText.text.trim().length
        if (accountNameLength !in Constants.ACCOUNT_NAME_LIMIT) {
            isValid = false
            accountNameEditText.addTextChangedListener(CharacterCountErrorWatcher(accountNameEditText, accountNameLimitTextView, Constants.ACCOUNT_NAME_LIMIT))
        }
        val authorNameLength = authorNameEditText.text.trim().length
        if (authorNameLength !in Constants.AUTHOR_NAME_LIMIT) {
            isValid = false
            authorNameEditText.addTextChangedListener(CharacterCountErrorWatcher(authorNameEditText, authorNameLimitTextView, Constants.AUTHOR_NAME_LIMIT))
        }

        val authorUrl = authorUrlEditText.text.trim().toString()
        when {
            !authorUrl.isEmpty() && !authorUrl.isUrl() -> {
                isValid = false
                authorUrlErrorTextView.text = getString(R.string.url_invalid)
                authorUrlErrorTextView.visibility = View.VISIBLE
                authorUrlErrorTextView.setTextColor(resources.color(R.color.error))
            }
            authorUrl.length !in Constants.AUTHOR_URL_LIMIT -> {
                isValid = false
                authorUrlEditText.addTextChangedListener(CharacterCountErrorWatcher(authorUrlEditText, authorUrlErrorTextView, Constants.AUTHOR_URL_LIMIT))
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
        findNavController().popBackStack()
    }

    override fun onLogout() {
        findNavController().navigate(NavigationGraphDirections.openLoginGlobalAction())
    }
}