package com.telex.base.presentation.pages

import android.view.View
import com.google.android.material.navigation.NavigationView
import com.telex.base.R
import com.telex.base.extention.inflate
import com.telex.base.extention.setGone
import com.telex.base.model.source.local.entity.User
import kotlinx.android.synthetic.main.layout_drawer_header.view.*

/**
 * @author Sergey Petrov
 */
abstract class BaseDrawerNavigationDelegate(
    drawerNavigationView: NavigationView
) {

    val drawerHeader = drawerNavigationView.getHeaderView(0)

    open fun setupDrawerHeaderDropDown() {
        val dropDownImageView = drawerHeader.dropDownImageView
        dropDownImageView.setGone(false)
        dropDownImageView.setOnClickListener {
            openAccountsLayout(drawerHeader.accountsLayout.visibility != View.VISIBLE)
        }

        openAccountsLayout(false)
    }

    protected fun openAccountsLayout(isVisible: Boolean) {
        with(drawerHeader) {
            accountsLayout.setGone(!isVisible)

            dropDownImageView.rotation = if (isVisible) 180f else 0f
        }
    }

    fun showAddAccountItem() {
        drawerHeader.accountsLayout.inflate(R.layout.item_add_account).apply {
            setOnClickListener {
                onAddAccountClicked()
                openAccountsLayout(false)
            }
        }.also { drawerHeader.accountsLayout.addView(it) }
    }

    abstract fun onAddAccountClicked()

    abstract fun showAccounts(users: List<User>)

    abstract fun showFooter()
}
