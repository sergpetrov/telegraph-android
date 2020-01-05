package com.telex.presentation.home

import android.content.Context
import com.google.android.material.navigation.NavigationView
import com.telex.model.source.local.entity.User

/**
 * @author Sergey Petrov
 */
class DrawerNavigationDelegate(
    val context: Context,
    val drawerNavigationView: NavigationView,
    private val onItemAccountClick: ((User) -> Unit)? = null
) : BaseDrawerNavigationDelegate(drawerNavigationView) {

    override fun showAccounts(users: List<User>) {
    }

    override fun onAddAccountClicked() {
    }

    override fun showFooter() {
    }

    private fun showUpgradeToPro() {
    }
}
