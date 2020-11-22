package com.telex.base.presentation.drawer

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Dialog
import android.os.Handler
import android.view.MotionEvent
import androidx.appcompat.app.AppCompatDelegate
import androidx.navigation.fragment.findNavController
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.internal.NavigationMenuView
import com.telex.base.NavigationGraphDirections
import com.telex.base.R
import com.telex.base.analytics.AnalyticsHelper
import com.telex.base.model.source.local.entity.User
import com.telex.base.presentation.base.BaseBottomSheetFragment
import com.telex.base.presentation.home.DrawerNavigationDelegate
import kotlinx.android.synthetic.main.fragment_bottom_navigation_drawer.*
import kotlinx.android.synthetic.main.layout_drawer_header.view.*
import kotlinx.android.synthetic.main.menu_item_drafts.view.*
import kotlinx.android.synthetic.main.menu_item_switch.view.*
import moxy.presenter.InjectPresenter
import moxy.presenter.ProvidePresenter

/**
 * @author Sergey Petrov
 */
class BottomNavigationDrawerFragment : BaseBottomSheetFragment(), BottomNavigationDrawerView {

    override val layout: Int = R.layout.fragment_bottom_navigation_drawer

    private val proxySwitch by lazy { requireDialog().navigationView.menu.findItem(R.id.proxyItem).actionView.itemSwitch }
    private val nightModeSwitch by lazy { requireDialog().navigationView.menu.findItem(R.id.nightModeItem).actionView.itemSwitch }
    private val drawerNavigationDelegate by lazy {
        DrawerNavigationDelegate(
                context = context,
                drawerNavigationView = requireDialog().navigationView,
                onItemAccountClick = { user ->
                    presenter.changeCurrentAccount(user)
                    dismiss()
                }
        )
    }

    @InjectPresenter
    lateinit var presenter: BottomNavigationDrawerPresenter

    @ProvidePresenter
    fun providePresenter(): BottomNavigationDrawerPresenter {
        return scope.getInstance(BottomNavigationDrawerPresenter::class.java)
    }

    override fun setupView(dialog: Dialog) {
        setupNavigationView()

        drawerNavigationDelegate.setupDrawerHeaderDropDown()
        drawerNavigationDelegate.drawerHeader.setOnClickListener {
            AnalyticsHelper.logOpenAccountSettings()
            findNavController().navigate(R.id.accountSettingsFragment)
            dismiss()
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun setupNavigationView() {
        val navigationMenuView = requireDialog().navigationView.getChildAt(0) as? NavigationMenuView
        if (navigationMenuView != null) {
            navigationMenuView.isVerticalScrollBarEnabled = false
        }

        requireDialog().navigationView.setNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.publishedItem -> {
                    if (findNavController().currentDestination?.id != R.id.pagesFragment) {
                        findNavController().popBackStack(R.id.pagesFragment, false)
                    }
                    dismiss()
                    true
                }
                R.id.draftsItem -> {
                    if (findNavController().currentDestination?.id != R.id.draftsFragment) {
                        findNavController().navigate(NavigationGraphDirections.openDraftsGlobalAction())
                    }
                    dismiss()
                    true
                }
                R.id.proxyItem -> {
                    AnalyticsHelper.logOpenProxy()
                    findNavController().navigate(R.id.proxyServerFragment)
                    dismiss()
                    true
                }
                R.id.nightModeItem -> {
                    presenter.switchNightMode(!nightModeSwitch.isChecked)
                    true
                }
                R.id.aboutItem -> {
                    findNavController().navigate(R.id.aboutAppFragment)
                    dismiss()
                    true
                }
                else -> false
            }
        }

        proxySwitch.setOnClickListener { presenter.enableProxyServer(proxySwitch.isChecked) }

        nightModeSwitch.setOnClickListener { presenter.switchNightMode(nightModeSwitch.isChecked) }
        nightModeSwitch.setOnTouchListener { v, event -> event.actionMasked == MotionEvent.ACTION_MOVE }

        drawerNavigationDelegate.showFooter()
    }


    override fun showProxyServerEnabled() {
        proxySwitch.isChecked = true
    }

    override fun showProxyServerDisabled() {
        proxySwitch.isChecked = false
    }

    override fun showProxyServerNotExist() {
        findNavController().navigate(R.id.proxyServerFragment)
        dismiss()
    }

    override fun updateNightMode(nightModeEnabled: Boolean, needRecreate: Boolean) {
        nightModeSwitch.isChecked = nightModeEnabled

        if (needRecreate) {
            val mode = if (nightModeEnabled) {
                AppCompatDelegate.MODE_NIGHT_YES
            } else {
                AppCompatDelegate.MODE_NIGHT_NO
            }

            Handler().postDelayed({
                AppCompatDelegate.setDefaultNightMode(mode)
                (context as Activity).recreate()
                dismiss()
            }, 200)
        }
    }

    override fun showCurrentAccount(user: User) {
        drawerNavigationDelegate.drawerHeader.titleTextView.text = user.accountName
        drawerNavigationDelegate.drawerHeader.subTitleTextView.text = resources.getQuantityString(R.plurals.d_posts, user.pageCount, user.pageCount)
    }

    override fun showAccounts(users: List<User>) {
        drawerNavigationDelegate.showAccounts(users)
    }

    override fun showDraftsCount(count: Int) {
        requireDialog().navigationView.menu
                .findItem(R.id.draftsItem)
                .actionView
                .draftsCountTextView.text = count.toString()
    }
}