package com.telex.base.analytics

import com.telex.base.BuildConfig
import com.telex.base.di.Scopes
import toothpick.Toothpick

/**
 * @author Sergey Petrov
 */
object AnalyticsHelper {
    private const val LAUNCH_TELEGRAM = "Launch Telegram"
    private const val LOGOUT = "Logout"
    private const val CREATE_PAGE = "Create Page"
    private const val EDIT_PAGE = "Edit Page"
    private const val EDIT_ACCOUNT_INFO = "Edit Account Info"
    private const val OPEN_ACCOUNT_SETTINGS = "Open Account Settings"
    private const val OPEN_PROXY = "Open Proxy"
    private const val SAVE_PROXY = "Save Proxy"
    private const val PROXY_ON = "Proxy On"
    private const val PROXY_OFF = "Proxy Off"
    private const val OPEN_PAGE_IN_BROWSER = "Open Page In Browser"
    private const val COPY_PAGE_LINK = "Copy Page Link"
    private const val SHARE_PAGE = "Share Page"
    private const val OPEN_ABOUT_DEVELOPER = "About Developer"
    private const val MOVE_BLOCK_UP = "Move Block Up"
    private const val MOVE_BLOCK_DOWN = "Move Block Down"
    private const val DELETE_BLOCK = "Delete Block"
    private const val DUPLICATE_BLOCK = "Duplicate Block"
    private const val OPEN_PAGE_STATISTICS = "Open Page Statistics"
    private const val CLICK_DELETE_POST = "Click Delete Post"
    private const val CLICK_TOP_BANNER = "Click Top Banner"
    private const val CLICK_ADD_ACCOUNT = "Click Add Account"
    private const val CLICK_DRAWER_UPGRADE_TO_PRO_BUTTON = "Click Drawer Upgrade To Pro Button"
    private const val APP_REVIEW_REQUESTED = "App Review Requested"

    private var analyticsReporter: AnalyticsReporter = Toothpick.openScope(Scopes.App).getInstance(AnalyticsReporter::class.java)

    private fun logEvent(eventKey: String) {
        if (!BuildConfig.DEBUG) {
            analyticsReporter.logEvent(eventKey)
        }
    }

    fun logLaunchTelegram() {
        logEvent(LAUNCH_TELEGRAM)
    }

    fun logLogout() {
        logEvent(LOGOUT)
    }

    fun logCreatePage() {
        logEvent(CREATE_PAGE)
    }

    fun logEditPage() {
        logEvent(EDIT_PAGE)
    }

    fun logEditAccountInfo() {
        logEvent(EDIT_ACCOUNT_INFO)
    }

    fun logOpenAccountSettings() {
        logEvent(OPEN_ACCOUNT_SETTINGS)
    }

    fun logSaveProxy() {
        logEvent(SAVE_PROXY)
    }

    fun logOpenProxy() {
        logEvent(OPEN_PROXY)
    }

    fun logProxyOn() {
        logEvent(PROXY_ON)
    }

    fun logProxyOff() {
        logEvent(PROXY_OFF)
    }

    fun logOpenPageInBrowser() {
        logEvent(OPEN_PAGE_IN_BROWSER)
    }

    fun logCopyPageLink() {
        logEvent(COPY_PAGE_LINK)
    }

    fun logSharePage() {
        logEvent(SHARE_PAGE)
    }

    fun logOpenAboutDeveloper() {
        logEvent(OPEN_ABOUT_DEVELOPER)
    }

    fun logMoveBlockUp() {
        logEvent(MOVE_BLOCK_UP)
    }

    fun logMoveBlockDown() {
        logEvent(MOVE_BLOCK_DOWN)
    }

    fun logDeleteBlock() {
        logEvent(DELETE_BLOCK)
    }

    fun logDuplicateBlock() {
        logEvent(DUPLICATE_BLOCK)
    }

    fun logOpenPageStatistics() {
        logEvent(OPEN_PAGE_STATISTICS)
    }

    fun logClickDeletePost() {
        logEvent(CLICK_DELETE_POST)
    }

    fun logTopBannerActionClick(title: String) {
        logEvent("$CLICK_TOP_BANNER action $title")
    }

    fun logClickAddAccount() {
        logEvent(CLICK_ADD_ACCOUNT)
    }

    fun logClickDrawerUpgradeToProButton() {
        logEvent(CLICK_DRAWER_UPGRADE_TO_PRO_BUTTON)
    }

    fun logAppReviewRequested() {
        logEvent(APP_REVIEW_REQUESTED)
    }
}
