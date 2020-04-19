package com.telex.base.presentation.base

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.annotation.LayoutRes
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.fragment.app.FragmentManager
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.telex.base.di.Scopes
import com.telex.base.extention.objectScopeName
import moxy.MvpAppCompatDialogFragment
import toothpick.Scope
import toothpick.Toothpick

/**
 * @author Sergey Petrov
 */
abstract class BaseBottomSheetFragment : MvpAppCompatDialogFragment() {

    protected var mBottomSheetBehaviorCallback: BottomSheetBehavior.BottomSheetCallback = object : BottomSheetBehavior.BottomSheetCallback() {
        override fun onStateChanged(bottomSheet: View, newState: Int) {
            this@BaseBottomSheetFragment.onStateChanged(bottomSheet, newState)
        }

        override fun onSlide(bottomSheet: View, slideOffset: Float) {
            this@BaseBottomSheetFragment.onSlide(bottomSheet, slideOffset)
        }
    }

    @get:LayoutRes
    abstract val layout: Int

    private var instanceStateSaved: Boolean = false

    protected open val parentScopeName: String by lazy {
        (parentFragment as? BaseFragment)?.scopeName ?: Scopes.App
    }

    protected open val scopeModuleInstaller: (Scope) -> Unit = {}

    private lateinit var scopeName: String
    protected lateinit var scope: Scope
        private set

    override fun getContext(): Context {
        return super.getContext() as Context
    }

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

    private fun isRealRemoving(): Boolean =
            (isRemoving && !instanceStateSaved) || // because isRemoving == true for fragment in backstack on screen rotation
                    ((parentFragment as? BaseFragment)?.isRealRemoving() ?: false)

    // It will be valid only for 'onDestroy()' method
    private fun needCloseScope(): Boolean =
            when {
                activity?.isChangingConfigurations == true -> false
                activity?.isFinishing == true -> true
                else -> isRealRemoving()
            }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return BottomSheetDialog(context, theme)
    }

    override fun setupDialog(dialog: Dialog, style: Int) {
        val contentView = View.inflate(context, layout, null)
        dialog.setContentView(contentView)

        val layoutParams = (contentView.parent as View).layoutParams as CoordinatorLayout.LayoutParams
        val behavior = layoutParams.behavior
        if (behavior != null && behavior is BottomSheetBehavior<*>) {
            behavior.setBottomSheetCallback(mBottomSheetBehaviorCallback)
        }

        setupView(dialog)
    }

    abstract fun setupView(dialog: Dialog)

    protected fun onStateChanged(bottomSheet: View, newState: Int) {
        if (newState == BottomSheetBehavior.STATE_HIDDEN) {
            dismiss()
        }
    }

    protected fun onSlide(bottomSheet: View, slideOffset: Float) {
    }

    fun showMessage(message: String) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }

    fun showError(message: String) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }

    fun showError(resourceId: Int) {
        Toast.makeText(context, getString(resourceId), Toast.LENGTH_SHORT).show()
    }

    fun show(fragmentManager: FragmentManager) {
        show(fragmentManager, tag)
    }

    companion object {
        private const val STATE_SCOPE_NAME = "state_scope_name"
        private const val STATE_SCOPE_WAS_CLOSED = "state_scope_was_closed"
    }
}
