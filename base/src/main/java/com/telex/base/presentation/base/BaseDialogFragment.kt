package com.telex.base.presentation.base

import android.content.Context
import android.widget.Toast
import com.telex.base.presentation.Router
import javax.inject.Inject
import moxy.MvpAppCompatDialogFragment

/**
 * @author Sergey Petrov
 */
abstract class BaseDialogFragment : MvpAppCompatDialogFragment() {

    @Inject
    lateinit var router: Router

    override fun getContext(): Context {
        return super.getContext() as Context
    }

    fun showError(message: String) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }

    fun showError(resourceId: Int) {
        Toast.makeText(context, getString(resourceId), Toast.LENGTH_SHORT).show()
    }

    fun onLogout() {
        router.showHomeActivity(context)
    }

    fun showProgress(isVisible: Boolean) {
    }
}
