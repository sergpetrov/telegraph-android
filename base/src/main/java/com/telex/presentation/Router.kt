package com.telex.presentation

import android.content.Context
import android.content.Intent
import androidx.fragment.app.FragmentManager
import com.telex.model.source.local.entity.Page
import com.telex.presentation.home.HomeActivity
import com.telex.presentation.page.EditorMode
import com.telex.presentation.page.PageActivity
import com.telex.presentation.settings.PrivacyPolicyActivity
import com.telex.presentation.settings.ProxyServerActivity
import javax.inject.Inject
import javax.inject.Singleton

/**
 * @author Sergey Petrov
 */
@Singleton
class Router @Inject constructor() {
    private var fragmentManager: FragmentManager? = null

    fun setup(fragmentManager: FragmentManager) {
        this.fragmentManager = fragmentManager
    }

    fun showHomeActivity(context: Context, clearStack: Boolean = true) {
        val intent = Intent(context, HomeActivity::class.java)
        if (clearStack) {
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        context.startActivity(intent)
    }

    fun clear() {
        fragmentManager = null
    }

    fun showNewPageActivity(context: Context, page: Page?, mode: EditorMode) {
        val intent = Intent(context, PageActivity::class.java)
        intent.putExtra(PageActivity.EXTRA_MODE, mode)
        page?.let {
            intent.putExtra(PageActivity.PAGE_ID, page.id)
            intent.putExtra(PageActivity.PAGE_AUTHOR_NAME, page.authorName)
            intent.putExtra(PageActivity.PAGE_AUTHOR_URL, page.authorUrl)
            intent.putExtra(PageActivity.PAGE_TITLE, page.title)
        }
        context.startActivity(intent)
    }

    fun showPrivacyPolicyActivity(context: Context) {
        context.startActivity(Intent(context, PrivacyPolicyActivity::class.java))
    }

    fun showProxyServerActivity(context: Context) {
        context.startActivity(Intent(context, ProxyServerActivity::class.java))
    }
}
