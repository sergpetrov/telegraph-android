package com.telex.presentation.base

import android.annotation.TargetApi
import android.content.Context
import android.os.Build
import android.util.AttributeSet
import android.view.View
import android.widget.FrameLayout
import android.widget.Toast
import androidx.annotation.CallSuper
import moxy.MvpDelegate

/**
 * @author Sergey Petrov
 */
abstract class BaseFrameLayout : FrameLayout {

    protected abstract val layoutRes: Int

    private var isCreated: Boolean = false
    private val mvpDelegate: MvpDelegate<out BaseFrameLayout> by lazy { MvpDelegate(this) }

    constructor(context: Context) : super(context) {
        init(context)
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        init(context, attrs)
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        init(context, attrs, defStyleAttr)
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int, defStyleRes: Int) : super(context, attrs, defStyleAttr, defStyleRes) {
        init(context, attrs, defStyleAttr, defStyleRes)
    }

    fun showError(message: String) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }

    fun showError(resourceId: Int) {
        Toast.makeText(context, context.getString(resourceId), Toast.LENGTH_SHORT).show()
    }

    @CallSuper
    protected open fun init(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0, defStyleRes: Int = 0) {
        View.inflate(context, layoutRes, this)

        if (!isCreated) {
            mvpDelegate.onCreate()
            isCreated = true
        }
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()

        if (!isCreated) {
            mvpDelegate.onCreate()
            isCreated = true
        }

        mvpDelegate.onAttach()
    }

    override fun onDetachedFromWindow() {
        mvpDelegate.onDetach()
        super.onDetachedFromWindow()
    }
}
