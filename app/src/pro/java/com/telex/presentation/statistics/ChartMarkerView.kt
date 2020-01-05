package com.telex.presentation.statistics

import android.content.Context
import com.github.mikephil.charting.components.MarkerView
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.utils.MPPointF
import com.telex.R
import kotlinx.android.synthetic.pro.layout_chart_marker.view.*

/**
 * @author Sergey Petrov
 */
class ChartMarkerView(context: Context) : MarkerView(context, R.layout.layout_chart_marker) {

    override fun refreshContent(entry: Entry, highlight: Highlight?) {
        super.refreshContent(entry, highlight)
        viewsTextView.text = entry.y.toInt().toString()
    }

    fun setTitle(text: String) {
        dateTextView.text = text
    }

    override fun getOffset(): MPPointF {
        return MPPointF((-(width / 2)).toFloat(), (-height - 50).toFloat())
    }
}
