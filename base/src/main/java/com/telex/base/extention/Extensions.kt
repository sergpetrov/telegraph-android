package com.telex.base.extention

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.res.ColorStateList
import android.content.res.Resources
import android.graphics.drawable.Drawable
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.text.Html
import android.util.Patterns
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.INVISIBLE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.view.WindowInsets
import android.view.animation.AnimationUtils
import android.view.animation.Interpolator
import android.view.inputmethod.InputMethodManager
import android.webkit.URLUtil
import android.widget.EditText
import android.widget.ImageView
import androidx.annotation.AttrRes
import androidx.annotation.LayoutRes
import androidx.annotation.StringRes
import androidx.core.content.PermissionChecker
import androidx.core.content.res.ResourcesCompat
import androidx.core.content.res.use
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.google.gson.Gson
import com.telex.base.R
import com.telex.base.model.source.remote.data.ErrorData
import com.telex.base.utils.Constants.ERROR_CONTENT_TEXT_REQUIRED
import com.telex.base.utils.Constants.ERROR_PAGE_ACCESS_DENIED
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import retrofit2.HttpException
import timber.log.Timber

/**
 * @author Sergey Petrov
 */
private val DATE_FORMAT = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())

fun String.isUrl(): Boolean {
    return Patterns.WEB_URL.matcher(this.trim()).matches()
}

fun String?.isHtmlBlank(): Boolean {
    return this.isNullOrBlank() || Html.fromHtml(this).toString().isBlank()
}

fun String.toNetworkUrl(): String {
    if (!this.isBlank() && !URLUtil.isNetworkUrl(this)) {
        return "http://" + this
    }
    return this
}

fun Date.format(): String {
    return DATE_FORMAT.format(this)
}

fun Context.color(colorRes: Int) =
        if (Build.VERSION.SDK_INT >= 23) {
            this.resources.getColor(colorRes, null)
        } else {
            this.resources.getColor(colorRes)
        }

fun Resources.color(colorRes: Int) =
        if (Build.VERSION.SDK_INT >= 23) {
            this.getColor(colorRes, null)
        } else {
            this.getColor(colorRes)
        }

fun Context.colorStateList(colorRes: Int): ColorStateList? {
    return ResourcesCompat.getColorStateList(resources, colorRes, null)
}

fun Context.getStringAsHtml(@StringRes stringRes: Int, vararg args: Any) =
        if (Build.VERSION.SDK_INT >= 24) {
            Html.fromHtml(this.getString(stringRes, *args), Html.FROM_HTML_MODE_COMPACT)
        } else {
            Html.fromHtml(this.getString(stringRes, *args))
        }

val Int.dp: Int
    get() = (this / Resources.getSystem().displayMetrics.density).toInt()
val Int.px: Int
    get() = (this * Resources.getSystem().displayMetrics.density).toInt()

fun Throwable.userMessage(resources: Resources): String = when (this) {
    is HttpException -> {
        val message: String
        val errorBody: String? = response()?.errorBody()?.string()
        if (errorBody != null) {
            message = try {
                val errorData = Gson().fromJson(errorBody, ErrorData::class.java)
                val errorMessage = errorData.error
                when (errorMessage) {
                    ERROR_PAGE_ACCESS_DENIED -> resources.getString(R.string.page_access_denied_error)
                    ERROR_CONTENT_TEXT_REQUIRED -> resources.getString(R.string.empty_content)
                    else -> errorMessage
                }
            } catch (error: Exception) {
                Timber.e(error, "errorBody=$errorBody")
                errorBody
            }
        } else {
            message = when (this.code()) {
                304 -> "304 Not Modified"
                400 -> "400 Bad Request"
                401 -> "401 Unauthorized"
                403 -> "403 Forbidden"
                404 -> "404 Not Found"
                405 -> "405 Method Not Allowed"
                409 -> "409 Conflict"
                422 -> "422 Unprocessable"
                500 -> resources.getString(R.string.server_error)
                else -> resources.getString(R.string.unknown_error)
            }
        }
        message
    }
    is IOException -> resources.getString(R.string.network_error)
    else -> resources.getString(R.string.unknown_error)
}

fun Activity.showKeyboard() {
    currentFocus?.let { currentFocus ->
        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.showSoftInput(currentFocus, InputMethodManager.SHOW_IMPLICIT)
    }
}

fun Activity.hideKeyboard() {
    currentFocus?.let { currentFocus ->
        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(currentFocus.windowToken, 0)
        currentFocus.clearFocus()
    }
}

inline var View.isVisible: Boolean
    get() = visibility == View.VISIBLE
    set(value) {
        visibility = if (value) View.VISIBLE else View.GONE
    }

fun View.setGone(isGone: Boolean) {
    visibility = if (isGone) GONE else VISIBLE
}

fun View.setInvisible(isInvisible: Boolean) {
    visibility = if (isInvisible) INVISIBLE else VISIBLE
}

fun EditText.requestFocusToEnd() {
    requestFocus()
    setSelection(text.length)
}

fun View.disable() {
    isEnabled = false
    alpha = 0.4f
}

fun View.enable() {
    isEnabled = true
    alpha = 1f
}

fun Context.getColorFromAttr(
        @AttrRes attrColor: Int,
        typedValue: TypedValue = TypedValue(),
        resolveRefs: Boolean = true
): Int {
    theme.resolveAttribute(attrColor, typedValue, resolveRefs)
    return typedValue.data
}

fun ViewGroup.inflate(@LayoutRes layoutRes: Int, attachToRoot: Boolean = false): View {
    return LayoutInflater.from(context).inflate(layoutRes, this, attachToRoot)
}

fun IntProgression.toTypedArray() = this.toList().toTypedArray()

fun Any.objectScopeName() = "${javaClass.simpleName}_${hashCode()}"

fun ImageView.loadImage(context: Context, url: String, onLoaded: (() -> Unit)? = null, onFailed: ((Throwable?) -> Unit)? = null) {
    Glide.with(context)
            .load(url)
            .fitCenter()
            .transition(DrawableTransitionOptions.withCrossFade())
            .diskCacheStrategy(DiskCacheStrategy.ALL)
            .addListener(object : RequestListener<Drawable> {
                override fun onLoadFailed(e: GlideException?, model: Any?, target: Target<Drawable>?, isFirstResource: Boolean): Boolean {
                    Timber.e(e)
                    onFailed?.invoke(e)
                    return false
                }

                override fun onResourceReady(resource: Drawable?, model: Any?, target: Target<Drawable>?, dataSource: DataSource?, isFirstResource: Boolean): Boolean {
                    onLoaded?.invoke()
                    return false
                }
            })
            .into(this)
}

fun ImageView.loadImage(context: Context, url: String, width: Int, height: Int, onLoaded: (() -> Unit)? = null, onFailed: ((Throwable?) -> Unit)? = null) {
    Glide.with(context)
            .load(url)
            .override(width, height)
            .transition(DrawableTransitionOptions.withCrossFade())
            .diskCacheStrategy(DiskCacheStrategy.ALL)
            .addListener(object : RequestListener<Drawable> {
                override fun onLoadFailed(e: GlideException?, model: Any?, target: Target<Drawable>?, isFirstResource: Boolean): Boolean {
                    Timber.e(e)
                    onFailed?.invoke(e)
                    return false
                }

                override fun onResourceReady(resource: Drawable?, model: Any?, target: Target<Drawable>?, dataSource: DataSource?, isFirstResource: Boolean): Boolean {
                    onLoaded?.invoke()
                    return false
                }
            })
            .into(this)
}

fun View.elevateOnScroll(recyclerView: RecyclerView) {
    val currentView = this
    recyclerView.addOnScrollListener(
            object : RecyclerView.OnScrollListener() {
                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    super.onScrolled(recyclerView, dx, dy)
                    currentView.elevation = if (recyclerView.canScrollVertically(-1)) recyclerView.context.resources.getDimension(R.dimen.toolbar_layout_elevation) * 1f else 0f
                }
            }
    )
}

@Suppress("DEPRECATION")
fun Context.isOnline(): Boolean {
    if (isPermissionsGranted(Manifest.permission.ACCESS_NETWORK_STATE)) {
        val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val capabilities = connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork)
            return if (capabilities != null) {
                when {
                    capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
                    capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
                    capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> true
                    else -> false
                }
            } else false
        } else {
            val activeNetworkInfo = connectivityManager.activeNetworkInfo ?: return false
            return activeNetworkInfo.isConnected
        }
    } else return false
}

fun Context.isPermissionsGranted(vararg permissions: String): Boolean {
    permissions.forEach { permission ->
        if (PermissionChecker.checkSelfPermission(this, permission) != PermissionChecker.PERMISSION_GRANTED) {
            return false
        }
    }
    return true
}

fun View.applySystemWindowInsetsPadding(
        applyLeft: Boolean = false,
        applyTop: Boolean = false,
        applyRight: Boolean = false,
        applyBottom: Boolean = false
) {
    doOnApplyWindowInsets { view, insets, padding, _, _ ->
        val left = if (applyLeft) insets.systemWindowInsetLeft else 0
        val top = if (applyTop) insets.systemWindowInsetTop else 0
        val right = if (applyRight) insets.systemWindowInsetRight else 0
        val bottom = if (applyBottom) insets.systemWindowInsetBottom else 0

        view.setPadding(
                padding.left + left,
                padding.top + top,
                padding.right + right,
                padding.bottom + bottom
        )
    }
}

fun View.doOnApplyWindowInsets(
        block: (View, WindowInsets, InitialPadding, InitialMargin, Int) -> Unit
) {
    // Create a snapshot of the view's padding & margin states
    val initialPadding = recordInitialPaddingForView(this)
    val initialMargin = recordInitialMarginForView(this)
    val initialHeight = recordInitialHeightForView(this)
    // Set an actual OnApplyWindowInsetsListener which proxies to the given
    // lambda, also passing in the original padding & margin states
    setOnApplyWindowInsetsListener { v, insets ->
        block(v, insets, initialPadding, initialMargin, initialHeight)
        // Always return the insets, so that children can also use them
        insets
    }
    // request some insets
    requestApplyInsetsWhenAttached()
}


class InitialPadding(val left: Int, val top: Int, val right: Int, val bottom: Int)

class InitialMargin(val left: Int, val top: Int, val right: Int, val bottom: Int)

private fun recordInitialPaddingForView(view: View) = InitialPadding(
        view.paddingLeft, view.paddingTop, view.paddingRight, view.paddingBottom
)

private fun recordInitialMarginForView(view: View): InitialMargin {
    val lp = view.layoutParams as? ViewGroup.MarginLayoutParams
            ?: throw IllegalArgumentException("Invalid view layout params")
    return InitialMargin(lp.leftMargin, lp.topMargin, lp.rightMargin, lp.bottomMargin)
}

private fun recordInitialHeightForView(view: View): Int {
    return view.layoutParams.height
}

fun View.requestApplyInsetsWhenAttached() {
    if (isAttachedToWindow) {
        // We're already attached, just request as normal
        requestApplyInsets()
    } else {
        // We're not attached to the hierarchy, add a listener to
        // request when we are
        addOnAttachStateChangeListener(object : View.OnAttachStateChangeListener {
            override fun onViewAttachedToWindow(v: View) {
                v.removeOnAttachStateChangeListener(this)
                v.requestApplyInsets()
            }

            override fun onViewDetachedFromWindow(v: View) = Unit
        })
    }
}

@SuppressLint("Recycle")
fun Context.themeInterpolator(@AttrRes attr: Int): Interpolator {
    return AnimationUtils.loadInterpolator(
            this,
            obtainStyledAttributes(intArrayOf(attr)).use {
                it.getResourceId(0, android.R.interpolator.fast_out_slow_in)
            }
    )
}
