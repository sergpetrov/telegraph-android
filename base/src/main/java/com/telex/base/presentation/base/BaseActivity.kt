package com.telex.base.presentation.base

import android.app.Dialog
import android.content.res.Configuration
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.os.Bundle
import android.view.KeyEvent
import android.view.View
import android.widget.Toast
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.widget.Toolbar
import com.telex.base.R
import com.telex.base.di.Scopes
import com.telex.base.extention.objectScopeName
import moxy.MvpAppCompatActivity
import toothpick.Scope
import toothpick.Toothpick

/**
 * @author Sergey Petrov
 */
abstract class BaseActivity : MvpAppCompatActivity() {

    abstract val layoutRes: Int
    private var overlayDialog: Dialog? = null

    private var instanceStateSaved: Boolean = false

    protected open val parentScopeName = Scopes.App

    protected open val scopeModuleInstaller: (Scope) -> Unit = {}

    private lateinit var scopeName: String
    protected lateinit var scope: Scope
        private set

    override fun onCreate(savedInstanceState: Bundle?) {
        window.apply {
            if (!isNightMode()) {
                decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_STABLE or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    decorView.systemUiVisibility = decorView.systemUiVisibility or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
                }

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    decorView.systemUiVisibility = decorView.systemUiVisibility or View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR
                }
            } else {
                decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_STABLE or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
            }
        }

        val scopeWasClosed = savedInstanceState?.getBoolean(STATE_SCOPE_WAS_CLOSED) ?: true

        scopeName = savedInstanceState?.getString(STATE_SCOPE_NAME) ?: objectScopeName()
        scope = Toothpick.openScopes(parentScopeName, scopeName)
                .apply {
                    if (scopeWasClosed) {
                        scopeModuleInstaller(this)
                    }
                }

        Toothpick.inject(this, scope)

        super.onCreate(savedInstanceState)

        setContentView(layoutRes)
    }

    override fun onResume() {
        super.onResume()
        instanceStateSaved = false
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        instanceStateSaved = true
        outState.putString(STATE_SCOPE_NAME, scopeName)
        outState.putBoolean(STATE_SCOPE_WAS_CLOSED, needCloseScope()) // save it but will be used only if destroyed
    }

    override fun onDestroy() {
        super.onDestroy()
        hideOverlay()

        if (needCloseScope()) {
            Toothpick.closeScope(scope.name)
        }
    }

    fun getToolbar(): Toolbar? {
        return findViewById(R.id.toolbar)
    }

    private fun needCloseScope(): Boolean =
            when {
                isChangingConfigurations -> false
                isFinishing -> true
                else -> true
            }

    fun setupStatusBar() {
        if (AppCompatDelegate.getDefaultNightMode() == AppCompatDelegate.MODE_NIGHT_NO) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_STABLE or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
            } else {
                window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_STABLE or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
            }
        } else {
            window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_STABLE or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
        }
    }

    fun showError(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    fun showError(resourceId: Int) {
        Toast.makeText(this, getString(resourceId), Toast.LENGTH_SHORT).show()
    }

    fun showMessage(@StringRes resourceId: Int) {
        Toast.makeText(this, getString(resourceId), Toast.LENGTH_SHORT).show()
    }

    fun showMessage(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    protected fun showOverlay() {
        if (overlayDialog == null) {
            overlayDialog = Dialog(this)
        }
        overlayDialog?.setContentView(R.layout.dialog_progress)
        overlayDialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        overlayDialog?.setCancelable(false)
        overlayDialog?.setOnKeyListener { dialog, keyCode, event ->
            if (keyCode == KeyEvent.KEYCODE_BACK) {
                dialog.dismiss()
                super.onBackPressed()
            }
            true
        }
        overlayDialog?.show()
    }

    protected fun hideOverlay() {
        overlayDialog?.dismiss()
    }

    protected fun isNightMode(): Boolean {
        return when (resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) {
            Configuration.UI_MODE_NIGHT_YES -> true
            Configuration.UI_MODE_NIGHT_NO -> false
            Configuration.UI_MODE_NIGHT_UNDEFINED -> false
            else -> false
        }
    }

    companion object {
        private const val STATE_SCOPE_NAME = "state_scope_name"
        private const val STATE_SCOPE_WAS_CLOSED = "state_scope_was_closed"
    }
}
