package com.telex.base.presentation

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.os.Bundle
import android.view.View
import android.view.ViewAnimationUtils
import android.view.ViewTreeObserver
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.NavGraph
import androidx.navigation.fragment.findNavController
import com.google.android.material.transition.Hold
import com.telex.base.R
import com.telex.base.model.interactors.UserInteractor
import com.telex.base.presentation.base.BaseActivity
import com.telex.base.presentation.drawer.BottomNavigationDrawerFragment
import com.telex.base.presentation.page.PageEditorFragmentDirections
import com.telex.base.presentation.pages.DraftsFragment
import com.telex.base.presentation.pages.PagesFragment
import kotlinx.android.synthetic.main.activity_app.*
import moxy.presenter.InjectPresenter
import moxy.presenter.ProvidePresenter

/**
 * @author Sergey Petrov
 */
class AppActivity : BaseActivity(), AppActivityView {

    override val layoutRes: Int = R.layout.activity_app

    private var navController: NavController? = null
    private lateinit var navGraph: NavGraph

    @InjectPresenter
    lateinit var presenter: AppActivityPresenter

    @ProvidePresenter
    fun providePresenter(): AppActivityPresenter {
        return scope.getInstance(AppActivityPresenter::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setupStatusBar()
        overridePendingTransition(0, 0)

        navController = supportFragmentManager.findFragmentById(R.id.fragmentContainerView)?.findNavController()
        navGraph = navController?.navInflater?.inflate(R.navigation.navigation_graph) ?: error("navGraph can't be null")

        if (savedInstanceState == null) {
            if (scope.getInstance(UserInteractor::class.java).isTokenValid()) {
                navGraph.startDestination = R.id.pagesFragment
            } else {
                navGraph.startDestination = R.id.loginFragment
            }
            navController?.graph = navGraph

            coordinatorLayout.visibility = View.INVISIBLE
            val viewTreeObserver = coordinatorLayout.viewTreeObserver
            if (viewTreeObserver.isAlive) {
                viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
                    override fun onGlobalLayout() {
                        circularRevealActivity()
                        coordinatorLayout.viewTreeObserver.removeOnGlobalLayoutListener(this)
                    }
                })
            }
        }

        bottomAppBarContentLayout.setOnClickListener {
            val bottomNavDrawerFragment = BottomNavigationDrawerFragment()
            bottomNavDrawerFragment.show(supportFragmentManager, bottomNavDrawerFragment.tag)
        }

        fab.apply {
            setShowMotionSpecResource(R.animator.fab_show)
            setHideMotionSpecResource(R.animator.fab_hide)
            setOnClickListener {
                navController?.navigate(PageEditorFragmentDirections.openPageEditorGlobalAction())
            }
        }

        navController?.addOnDestinationChangedListener { controller, destination, _ ->
            val currentFragment = getCurrentFragment()
            if (currentFragment is PagesFragment || currentFragment is DraftsFragment) {
                if (destination.id == R.id.pageEditorFragment) {
                    currentFragment.exitTransition = Hold().apply {
                        duration = resources.getInteger(R.integer.motion_default_large).toLong()
                    }
                } else {
                    currentFragment.exitTransition = null
                }
            }
            when (destination.id) {
                R.id.loginFragment,
                R.id.pageEditorFragment,
                R.id.aboutAppFragment,
                R.id.privacyPolicyFragment,
                R.id.proxyServerFragment,
                R.id.accountSettingsFragment -> hideBottomAppBar()
                R.id.pagesFragment -> {
                    showBottomAppBar()
                    bottomAppBarTitleTextView.setText(R.string.published)
                }
                R.id.draftsFragment -> {
                    showBottomAppBar()
                    bottomAppBarTitleTextView.setText(R.string.drafts)
                }
            }
        }
    }

    private fun circularRevealActivity() {
        val cx = coordinatorLayout.width / 2
        val cy = coordinatorLayout.height / 2
        val finalRadius = coordinatorLayout.width.coerceAtLeast(coordinatorLayout.height)
        val circularReveal = ViewAnimationUtils.createCircularReveal(coordinatorLayout, cx, cy, 0f, finalRadius.toFloat())
        circularReveal.duration = 700
        coordinatorLayout.visibility = View.VISIBLE
        circularReveal.start()
    }

    private fun showBottomAppBar() {
        fab.setImageState(intArrayOf(-android.R.attr.state_activated), true)
        bottomAppBar.visibility = View.VISIBLE
        bottomAppBar.performShow()
        fab.show()
    }

    private fun hideBottomAppBar() {
        bottomAppBar.performHide()
        fab.hide()
        bottomAppBar.animate().setListener(object : AnimatorListenerAdapter() {
            var isCanceled = false
            override fun onAnimationEnd(animation: Animator?) {
                if (isCanceled) return
                bottomAppBar.visibility = View.GONE
                fab.visibility = View.GONE
            }

            override fun onAnimationCancel(animation: Animator?) {
                isCanceled = true
            }
        })
    }

    private fun getCurrentFragment(): Fragment? {
        return supportFragmentManager.findFragmentById(R.id.fragmentContainerView)?.childFragmentManager?.primaryNavigationFragment
    }
}