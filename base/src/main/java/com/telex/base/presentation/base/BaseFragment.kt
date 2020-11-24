package com.telex.base.presentation.base

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import com.telex.base.R
import com.telex.base.di.Scopes
import com.telex.base.extention.objectScopeName
import moxy.MvpAppCompatFragment
import toothpick.Scope
import toothpick.Toothpick

/**
 * @author Sergey Petrov
 */
abstract class BaseFragment : MvpAppCompatFragment() {

    abstract val layoutRes: Int
    private var overlayDialog: Dialog? = null

    private var instanceStateSaved: Boolean = false

    protected open val parentScopeName: String by lazy {
        (parentFragment as? BaseFragment)?.scopeName ?: Scopes.App
    }

    protected open val scopeModuleInstaller: (Scope) -> Unit = {}

    lateinit var scopeName: String
    protected lateinit var scope: Scope
        private set

    override fun getContext(): Context {
        return super.getContext() as Context
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?) = inflater.inflate(layoutRes, container, false)

    override fun onCreate(savedInstanceState: Bundle?) {
        val scopeWasClosed = savedInstanceState?.getBoolean(STATE_SCOPE_WAS_CLOSED) ?: true
        scopeName = savedInstanceState?.getString(STATE_SCOPE_NAME) ?: objectScopeName()
        scope = Toothpick.openScopes(parentScopeName, scopeName)
                .apply {
                    if (scopeWasClosed) {
                        scopeModuleInstaller(this)
                    }
                }

        super.onCreate(savedInstanceState)
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

    override fun onDestroyView() {
        super.onDestroyView()
        hideOverlay()
    }

    override fun onDestroy() {
        super.onDestroy()
        hideOverlay()

        if (needCloseScope()) {
            Toothpick.closeScope(scope.name)
        }
    }

    protected fun getToolbar(): Toolbar? {
        return view?.findViewById(R.id.toolbar)
    }

    fun isRealRemoving(): Boolean =
            (isRemoving && !instanceStateSaved) || // because isRemoving == true for fragment in backstack on screen rotation
                    ((parentFragment as? BaseFragment)?.isRealRemoving() ?: false)

    // It will be valid only for 'onDestroy()' method
    private fun needCloseScope(): Boolean =
            when {
                activity?.isChangingConfigurations == true -> false
                activity?.isFinishing == true -> true
                else -> isRealRemoving()
            }

    fun showError(message: String) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }

    fun showError(resourceId: Int) {
        Toast.makeText(context, getString(resourceId), Toast.LENGTH_SHORT).show()
    }

    protected fun showOverlay() {
        if (overlayDialog == null) {
            overlayDialog = Dialog(context)
            overlayDialog?.setContentView(R.layout.dialog_progress)
            overlayDialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            overlayDialog?.setCancelable(false)
            overlayDialog?.show()
        }
    }

    protected fun hideOverlay() {
        overlayDialog?.hide()
        overlayDialog = null
    }

    companion object {
        private const val STATE_SCOPE_NAME = "state_scope_name"
        private const val STATE_SCOPE_WAS_CLOSED = "state_scope_was_closed"
    }
}
