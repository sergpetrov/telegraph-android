package com.telex.base.presentation.base

import android.content.Context
import android.util.AttributeSet
import com.google.android.material.textfield.TextInputLayout
import com.telex.base.R
import com.telex.base.extention.colorStateList
import com.telex.base.extention.isUrl

/**
 * @author Sergey Petrov
 */
class UrlTextInputLayout : TextInputLayout {

    constructor(context: Context) : super(context, null)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs, 0)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    init {
        setErrorTextColor(context.colorStateList(R.color.error))
        addOnEditTextAttachedListener {
            if (editText?.hint.isNullOrEmpty()) {
                editText?.hint = "https://"
            }
        }
    }

    fun isInputValid(condition: (() -> Boolean)? = null): Boolean {
        editText?.let {
            val value = it.text.toString()
            val isValid = value.isUrl() && (condition == null || condition.invoke())
            error = context.getString(R.string.url_invalid)
            isErrorEnabled = !isValid
            return isValid
        }
        return false
    }
}
