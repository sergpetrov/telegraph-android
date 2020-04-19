package com.telex.base.presentation.splash

import android.content.Intent
import android.os.Bundle
import android.view.View.GONE
import android.view.View.VISIBLE
import com.telex.base.R
import com.telex.base.presentation.AppActivity
import com.telex.base.presentation.base.BaseActivity
import kotlinx.android.synthetic.main.activity_splash.*
import moxy.presenter.InjectPresenter
import moxy.presenter.ProvidePresenter

/**
 * @author Sergey Petrov
 */
class SplashActivity : BaseActivity(), SplashView {

    override val layoutRes: Int = R.layout.activity_splash

    @InjectPresenter
    lateinit var presenter: SplashPresenter

    @ProvidePresenter
    fun providePresenter(): SplashPresenter {
        return scope.getInstance(SplashPresenter::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setupStatusBar()
        presenter.launch(intent.data)
    }

    override fun showProgress(isVisible: Boolean) {
        if (isVisible) {
            progressBar.visibility = VISIBLE
        } else {
            progressBar.visibility = GONE
        }
    }

    override fun showNext() {
        showAppActivity()
        finish()
        overridePendingTransition(0, 0)
    }

    override fun onLogout() {
        showAppActivity()
        finish()
        overridePendingTransition(0, 0)
    }

    private fun showAppActivity() {
        val intent = Intent(this, AppActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
        startActivity(intent)
    }
}
