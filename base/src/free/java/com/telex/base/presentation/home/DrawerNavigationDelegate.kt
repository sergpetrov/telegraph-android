package com.telex.base.presentation.home

import android.content.Context
import android.content.Intent
import android.view.View
import android.view.ViewGroup
import com.google.android.material.navigation.NavigationView
import com.telex.base.R
import com.telex.base.analytics.AnalyticsHelper
import com.telex.base.model.source.local.entity.User
import com.telex.base.presentation.pages.BaseDrawerNavigationDelegate

/**
 * @author Sergey Petrov
 */
class DrawerNavigationDelegate(
    val context: Context,
    val drawerNavigationView: NavigationView,
    private val onItemAccountClick: ((User) -> Unit)? = null
) : BaseDrawerNavigationDelegate(drawerNavigationView) {

    override fun showAccounts(users: List<User>) {
        val accountsLayout = drawerHeader.findViewById<ViewGroup>(R.id.accountsLayout)
        accountsLayout.removeAllViews()

        showAddAccountItem()
    }

    override fun onAddAccountClicked() {
        AnalyticsHelper.logClickAddAccount()
        showUpgradeToPro()
    }

    override fun showFooter() {
        val upgradeToProButton = View.inflate(context, R.layout.layout_upgrade_to_pro, null)
        drawerNavigationView.findViewById<ViewGroup>(R.id.navigationFooterLayout).addView(upgradeToProButton)
        upgradeToProButton.setOnClickListener {
            AnalyticsHelper.logClickDrawerUpgradeToProButton()
            showUpgradeToPro()
        }
    }

    private fun showUpgradeToPro() {
        context.startActivity(Intent(context, UpgradeToProActivity::class.java))
    }
}
