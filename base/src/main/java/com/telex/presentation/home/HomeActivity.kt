package com.telex.presentation.home

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.text.style.ClickableSpan
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewAnimationUtils
import android.view.ViewTreeObserver
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.view.GravityCompat
import com.google.android.material.internal.NavigationMenuView
import com.google.android.material.tabs.TabLayout
import com.telex.R
import com.telex.analytics.AnalyticsHelper
import com.telex.extention.setGone
import com.telex.model.source.local.entity.User
import com.telex.presentation.base.BaseActivity
import com.telex.presentation.page.EditorMode.Create
import com.telex.presentation.settings.AboutActivity
import com.telex.presentation.settings.AccountSettingsActivity
import com.telex.utils.ViewUtils
import kotlinx.android.synthetic.main.activity_home.*
import kotlinx.android.synthetic.main.layout_drawer_header.view.*
import kotlinx.android.synthetic.main.layout_unauthorized.view.*
import kotlinx.android.synthetic.main.menu_item_switch.view.*
import moxy.presenter.InjectPresenter
import moxy.presenter.ProvidePresenter

/**
 * @author Sergey Petrov
 */
class HomeActivity : BaseActivity(), HomeView {

    override val layoutRes: Int = R.layout.activity_home

    private val proxySwitch by lazy { navigationView.menu.findItem(R.id.proxyItem).actionView.itemSwitch }
    private val nightModeSwitch by lazy { navigationView.menu.findItem(R.id.nightModeItem).actionView.itemSwitch }
    private val drawerNavigationDelegate by lazy {
        DrawerNavigationDelegate(
                context = this,
                drawerNavigationView = navigationView,
                onItemAccountClick = { user ->
                    presenter.changeCurrentAccount(user)
                    drawerLayout.closeDrawer(GravityCompat.START)
                }
        )
    }

    @InjectPresenter
    lateinit var presenter: HomePresenter

    @ProvidePresenter
    fun providePresenter(): HomePresenter {
        return scope.getInstance(HomePresenter::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setupStatusBar()
        overridePendingTransition(0, 0)

        if (savedInstanceState == null) {
            rootLayout.visibility = View.INVISIBLE
            val viewTreeObserver = rootLayout.viewTreeObserver
            if (viewTreeObserver.isAlive) {
                viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
                    override fun onGlobalLayout() {
                        circularRevealActivity()
                        rootLayout.viewTreeObserver.removeOnGlobalLayoutListener(this)
                    }
                })
            }
        }

        setupNavigationView()
    }

    override fun onResume() {
        super.onResume()
        router.setup(supportFragmentManager)
    }

    override fun onPause() {
        super.onPause()
        router.clear()
    }

    override fun showProxyServerEnabled() {
        proxySwitch.isChecked = true
    }

    override fun showProxyServerDisabled() {
        proxySwitch.isChecked = false
    }

    override fun showProxyServerNotExist() {
        router.showProxyServerActivity(this)
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun setupNavigationView() {
        val navigationMenuView = navigationView.getChildAt(0) as? NavigationMenuView
        if (navigationMenuView != null) {
            navigationMenuView.isVerticalScrollBarEnabled = false
        }

        menuButton.setOnClickListener { drawerLayout.openDrawer(GravityCompat.START) }

        navigationView.setNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.proxyItem -> {
                    AnalyticsHelper.logOpenProxy()

                    router.showProxyServerActivity(this)
                    true
                }
                R.id.nightModeItem -> {
                    presenter.switchNightMode(!nightModeSwitch.isChecked)
                    true
                }
                R.id.aboutItem -> {
                    startActivity(Intent(this, AboutActivity::class.java))
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
                recreate()
            }, 200)
        }
    }

    private fun circularRevealActivity() {
        val cx = rootLayout.width / 2
        val cy = rootLayout.height / 2
        val finalRadius = rootLayout.width.coerceAtLeast(rootLayout.height)
        val circularReveal = ViewAnimationUtils.createCircularReveal(rootLayout, cx, cy, 0f, finalRadius.toFloat())
        circularReveal.duration = 700
        rootLayout.visibility = VISIBLE
        circularReveal.start()
    }

    override fun setupUnauthorized() {
        toolbar.elevation = 0f

        menuLayout.visibility = GONE
        clearMessageLayout()

        val view = LayoutInflater.from(this).inflate(R.layout.layout_unauthorized, null)

        ViewUtils.createLink(view.privacyPolicyDescriptionTextView, getString(R.string.privacy_policy_description), getString(R.string.privacy_policy_description_part_to_click), clickableAction = object : ClickableSpan() {
            override fun onClick(widget: View) {
                router.showPrivacyPolicyActivity(this@HomeActivity)
            }
        })

        showMessageView(view)
        view.launchButton.setOnClickListener {
            AnalyticsHelper.logLaunchTelegram()
            ViewUtils.openTelegram(this)
        }
    }

    override fun setupAuthorized() {
        toolbar.elevation = resources.getDimension(R.dimen.toolbar_layout_elevation)

        drawerNavigationDelegate.setupDrawerHeaderDropDown()
        drawerNavigationDelegate.drawerHeader.setOnClickListener {
            AnalyticsHelper.logOpenAccountSettings()

            startActivity(Intent(this, AccountSettingsActivity::class.java))
        }

        menuLayout.visibility = VISIBLE
        clearMessageLayout()

        newPageImageView.setOnClickListener {
            router.showNewPageActivity(this, null, Create)
        }

        val pagerAdapter = ViewPagerAdapter(supportFragmentManager)
        pagerAdapter.addFragment(PagesFragment.newInstance(false), getString(R.string.published))
        pagerAdapter.addFragment(PagesFragment.newInstance(true), getString(R.string.drafts))
        viewPager.adapter = pagerAdapter
        viewPager.offscreenPageLimit = 2

        tabLayout.setupWithViewPager(viewPager)
        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                viewPager.currentItem = tab.position
            }

            override fun onTabUnselected(tab: TabLayout.Tab) {
            }

            override fun onTabReselected(tab: TabLayout.Tab) {
            }
        })
    }

    override fun showCurrentAccount(user: User) {
        drawerNavigationDelegate.drawerHeader.titleTextView.text = user.accountName
        drawerNavigationDelegate.drawerHeader.subTitleTextView.text = resources.getQuantityString(R.plurals.d_posts, user.pageCount, user.pageCount)
    }

    override fun showAccounts(users: List<User>) {
        drawerNavigationDelegate.showAccounts(users)
    }

    override fun showDraftsCount(count: Int) {
        var title = getString(R.string.drafts)
        if (count > 0) {
            title += " $count"
        }
        tabLayout.getTabAt(1)?.text = title
    }

    private fun showMessageView(view: View) {
        messageLayout.addView(view)
        messageLayout.setGone(false)
    }

    private fun clearMessageLayout() {
        messageLayout.setGone(true)
        messageLayout.removeAllViews()
    }

    override fun showProgress(isVisible: Boolean) {
    }

    override fun onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }
}
