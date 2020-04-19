package com.telex.base.presentation.base

import android.content.Context
import android.widget.Toast
import androidx.fragment.app.FragmentManager
import moxy.MvpAppCompatDialogFragment

/**
 * @author Sergey Petrov
 */
abstract class BaseDialogFragment : MvpAppCompatDialogFragment() {

    override fun getContext(): Context {
        return super.getContext() as Context
    }

    fun showError(message: String) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }

    fun showError(resourceId: Int) {
        Toast.makeText(context, getString(resourceId), Toast.LENGTH_SHORT).show()
    }

    fun showProgress(isVisible: Boolean) {
    }

    fun show(fragmentManager: FragmentManager) {
        show(fragmentManager, tag)
    }
}
