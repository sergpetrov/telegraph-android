package com.telex.ui

import android.annotation.TargetApi
import android.content.Context
import android.os.Build
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import androidx.appcompat.widget.TooltipCompat
import com.telex.R
import kotlinx.android.synthetic.main.layout_format_toogle_button.view.*

/**
 * @author Sergey Petrov
 */
class FormatToggleButton : FrameLayout {

    var isChecked: Boolean
        get() = checkBox.isChecked
        set(value) {
            checkBox.isChecked = value
        }

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
        val view = LayoutInflater.from(context).inflate(R.layout.layout_format_toogle_button, this, true)
        val a = context.obtainStyledAttributes(attrs, R.styleable.FormatToggleButton, defStyleAttr, defStyleRes)

        val icon = a.getResourceId(R.styleable.FormatToggleButton_icon, -1)
        iconImageView.setImageResource(icon)

        a.recycle()
    }

    override fun setOnClickListener(listener: OnClickListener?) {
        checkBox.setOnClickListener(listener)
    }

    override fun setEnabled(enabled: Boolean) {
        super.setEnabled(enabled)
        checkBox.isEnabled = enabled
    }

    override fun setTooltipText(tooltipText: CharSequence?) {
        TooltipCompat.setTooltipText(checkBox, tooltipText)
    }
}
