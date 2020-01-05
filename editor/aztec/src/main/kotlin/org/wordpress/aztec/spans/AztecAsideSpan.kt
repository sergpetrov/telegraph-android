package org.wordpress.aztec.spans

import android.graphics.Paint
import android.text.style.LineHeightSpan
import android.text.style.UpdateLayout
import org.wordpress.aztec.AztecAttributes

class AztecAsideSpan (
        override var nestingLevel: Int,
        override var attributes: AztecAttributes
    ) : IAztecLineBlockSpan, LineHeightSpan, UpdateLayout {

    override val TAG: String
        get() = "aside"

    override fun chooseHeight(text: CharSequence?, start: Int, end: Int, spanstartv: Int, v: Int, fm: Paint.FontMetricsInt?) {

    }

    override var endBeforeBleed: Int = -1
    override var startBeforeCollapse: Int = -1

    override fun toString() = "AztecAsideSpan : $TAG"
}
