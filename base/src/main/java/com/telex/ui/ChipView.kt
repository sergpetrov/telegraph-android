package com.telex.ui

import android.annotation.TargetApi
import android.content.Context
import android.os.Build
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import com.telex.R
import com.telex.extention.setGone
import kotlinx.android.synthetic.main.layout_chip.view.*

/**
 * @author Sergey Petrov
 */
class ChipView : FrameLayout {

    constructor(context: Context) : super(context) {
        init(context, null, 0, 0)
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        init(context, attrs, 0, 0)
    }

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        init(context, attrs, defStyleAttr, 0)
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int, defStyleRes: Int) : super(context, attrs, defStyleAttr, defStyleRes) {
        init(context, attrs, defStyleAttr, defStyleRes)
    }

    protected fun init(context: Context, attrs: AttributeSet?, defStyleAttr: Int, defStyleRes: Int) {
        val view = LayoutInflater.from(context).inflate(R.layout.layout_chip, this, true)
        val a = context.obtainStyledAttributes(attrs, R.styleable.ChipView, defStyleAttr, defStyleRes)

        val closable = a.getBoolean(R.styleable.ChipView_closable, false)
        val title = a.getText(R.styleable.ChipView_title)

        setTitle(title.toString())
        setClosable(closable)

        a.recycle()
    }

    fun setTitle(text: String) {
        titleTextView.text = text
    }

    fun setClosable(closable: Boolean) {
        closeImageView.setGone(!closable)
        if (closable) {
            titleTextView.setPadding(titleTextView.paddingLeft, 0, 0, 0)
        } else {
            titleTextView.setPadding(titleTextView.paddingLeft, 0, titleTextView.paddingLeft, 0)
        }
    }

    fun setOnCloseClickListener(onClick: () -> Unit) {
        closeImageView.setOnClickListener { onClick.invoke() }
    }
}
