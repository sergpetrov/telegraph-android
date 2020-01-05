package com.telex.presentation.home

import android.app.Activity
import android.content.Context
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.navigation.NavigationView
import com.telex.R
import com.telex.analytics.AnalyticsHelper
import com.telex.extention.inflate
import com.telex.model.source.local.AppData
import com.telex.model.source.local.entity.User
import com.telex.utils.ViewUtils

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

        users.forEach { user ->
            accountsLayout.inflate(R.layout.item_account).apply {
                findViewById<TextView>(R.id.accountNameTextView).text = user.accountName
                findViewById<TextView>(R.id.postsCountTextView).text = resources.getQuantityString(R.plurals.d_posts, user.pageCount, user.pageCount)
                setOnClickListener {
                    onItemAccountClick?.invoke(user)
                    openAccountsLayout(false)
                }
            }.also { accountsLayout.addView(it) }
        }

        showAddAccountItem()
    }

    override fun onAddAccountClicked() {
        AnalyticsHelper.logClickAddAccount()

        val appData = AppData(context)
        if (appData.needShowAddAccountDialog()) {
            val fragmentManager = (context as AppCompatActivity).supportFragmentManager
            AddAccountDialogFragment.newInstance(
                    onLaunchClickListener = {
                        appData.putNeedShowAddAccountDialog(need = false)
                        openTelegram()
                    }
            ).apply { show(fragmentManager, tag) }
        } else {
            openTelegram()
        }
    }

    override fun showFooter() {
    }

    private fun openTelegram() {
        ViewUtils.openTelegram(context as Activity)
    }
}
