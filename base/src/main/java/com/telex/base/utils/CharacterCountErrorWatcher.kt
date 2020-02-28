package com.telex.base.utils

import android.text.Editable
import android.text.TextWatcher
import android.view.View.VISIBLE
import android.widget.EditText
import android.widget.TextView
import com.telex.base.R
import com.telex.base.extention.color
import com.telex.base.extention.requestFocusToEnd

/**
 * @author Sergey Petrov
 */
class CharacterCountErrorWatcher(private val editText: EditText, private val textView: TextView, private val limitRange: IntRange) : TextWatcher {

    init {
        updateErrorText()
    }

    private fun updateErrorText() {
        val length = editText.text.length
        if (hasValidLength()) {
            textView.setTextColor(textView.resources.color(R.color.secondary_text_color))
        } else {
            editText.requestFocusToEnd()

            textView.visibility = VISIBLE
            textView.setTextColor(textView.resources.color(R.color.error))
        }
        textView.text = "$length / ${limitRange.endInclusive}"
    }

    private fun hasValidLength(): Boolean {
        val length = editText.text.length
        return length in limitRange
    }

    override fun afterTextChanged(s: Editable) {
        updateErrorText()
    }

    override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
    }

    override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
    }
}
