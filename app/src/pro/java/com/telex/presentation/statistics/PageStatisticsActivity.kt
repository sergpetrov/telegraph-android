package com.telex.presentation.statistics

import android.os.Bundle
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.IValueFormatter
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.listener.OnChartValueSelectedListener
import com.telex.R
import com.telex.extention.color
import com.telex.extention.disable
import com.telex.extention.enable
import com.telex.extention.setGone
import com.telex.presentation.base.BaseActivity
import com.telex.utils.DateUtils
import kotlinx.android.synthetic.pro.activity_page_statistics.*
import moxy.presenter.InjectPresenter
import moxy.presenter.ProvidePresenter

/**
 * @author Sergey Petrov
 */
class PageStatisticsActivity : BaseActivity(), PageStatisticsView {

    override val layoutRes: Int
        get() = R.layout.activity_page_statistics

    private val pagePath: String by lazy {
        intent?.getStringExtra(PageStatisticsActivity.PAGE_PATH)
                ?: throw IllegalArgumentException("pagePath can't be null")
    }

    private var markerView: ChartMarkerView? = null

    @InjectPresenter
    lateinit var presenter: PageStatisticsPresenter

    @ProvidePresenter
    fun providePresenter(): PageStatisticsPresenter {
        return scope.getInstance(PageStatisticsPresenter::class.java).apply {
            pagePath = this@PageStatisticsActivity.pagePath
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setupStatusBar()
        setupChart()
        closeImageView.setOnClickListener { finish() }

        prevTextView.setOnClickListener {
            presenter.loadPageViewsForPrevPeriod()
        }

        nextTextView.setOnClickListener {
            presenter.loadPageViewsForNextPeriod()
        }

        yearChipView.setOnClickListener {
            presenter.onYearClicked()
        }

        monthChipView.setOnClickListener {
            presenter.onMonthClicked()
        }

        monthChipView.setOnCloseClickListener {
            presenter.changeStatisticsType(StatisticsType.Year)
            presenter.loadPageViews()
        }
    }

    override fun updateForMonthStatisticsType() {
        prevTextView.setText(R.string.prev_month)
        nextTextView.setText(R.string.next_month)

        chart.highlightValue(null)
        chart.xAxis.setValueFormatter { value, axis -> "${DateUtils.getMonthName(this, presenter.month, short = true)} ${value.toInt()}" }
        chart.setOnChartValueSelectedListener(object : OnChartValueSelectedListener {
            override fun onNothingSelected() {
            }

            override fun onValueSelected(entry: Entry, h: Highlight) {
                markerView?.setTitle("${DateUtils.getMonthName(this@PageStatisticsActivity, presenter.month, short = true)} ${entry.x.toInt()}")
            }
        })
    }

    override fun updateForYearStatisticsType() {
        resetMonth()

        prevTextView.setText(R.string.prev_year)
        nextTextView.setText(R.string.next_year)

        chart.highlightValue(null)
        chart.xAxis.setValueFormatter { value, axis -> DateUtils.getMonthName(this, value.toInt() + 1, short = true) }
        chart.setOnChartValueSelectedListener(object : OnChartValueSelectedListener {
            override fun onNothingSelected() {
            }

            override fun onValueSelected(entry: Entry, h: Highlight) {
                markerView?.setTitle(DateUtils.getMonthName(this@PageStatisticsActivity, entry.x.toInt() + 1, short = false))
            }
        })
    }

    private fun resetMonth() {
        monthChipView.setTitle(getString(R.string.month))
        monthChipView.setClosable(false)
        monthChipView.alpha = 0.4f
    }

    private fun setupChart() {
        chart.axisRight.isEnabled = false
        chart.description.isEnabled = false
        chart.legend.isEnabled = false
        chart.setScaleEnabled(false)
        chart.extraBottomOffset = 10f
        markerView = ChartMarkerView(this)
        chart.marker = markerView

        chart.setOnChartValueSelectedListener(object : OnChartValueSelectedListener {
            override fun onNothingSelected() {
            }

            override fun onValueSelected(entry: Entry, h: Highlight) {
            }
        })

        chart.axisLeft.setDrawAxisLine(false)
        chart.axisLeft.setValueFormatter { value, axis -> "${value.toInt()}" }
        chart.axisLeft.textSize = 12f
        chart.axisLeft.textColor = resources.color(R.color.secondary_text_color)
        chart.axisLeft.spaceBottom = 0f
        chart.axisLeft.xOffset = 10f
        chart.axisLeft.axisMinimum = 0f
        chart.axisLeft.isGranularityEnabled = true
        chart.axisLeft.axisLineWidth = 1f

        chart.xAxis.position = XAxis.XAxisPosition.BOTTOM
        chart.xAxis.setDrawGridLines(false)
        chart.xAxis.textSize = 12f
        chart.xAxis.setAvoidFirstLastClipping(true)
        chart.xAxis.textColor = resources.color(R.color.secondary_text_color)
        chart.xAxis.setLabelCount(5, true)
    }

    override fun showPageViewsSet(values: Map<Int, Int>) {
        chart.setGone(false)

        val entries = ArrayList<Entry>()
        values.mapTo(entries) { entry -> Entry(entry.key.toFloat(), entry.value.toFloat()) }

        val dataSet = LineDataSet(entries, "")
        dataSet.setDrawCircles(false)
        dataSet.setDrawValues(false)
        dataSet.lineWidth = 2f
        dataSet.highlightLineWidth = 2f
        dataSet.highLightColor = resources.color(R.color.light_grey)
        dataSet.color = resources.color(R.color.chart_color)
        dataSet.setDrawHorizontalHighlightIndicator(false)
        dataSet.valueFormatter = IValueFormatter { value, _, _, _ -> "${value.toInt()}" }

        val data = LineData(dataSet)
        chart.data = data
        chart.notifyDataSetChanged()
        chart.invalidate()
    }

    override fun showPageViews(viewsCount: Int) {
        viewsTextView.text = viewsCount.toString()
    }

    override fun showPageTotalViews(viewsCount: Int) {
        totalViewsTextView.text = viewsCount.toString()
    }

    override fun showYear(year: Int) {
        yearChipView.setTitle(year.toString())
    }

    override fun showMonth(month: Int) {
        monthChipView.setTitle(DateUtils.getMonthName(this, month, short = false))
        monthChipView.alpha = 1f
        monthChipView.setClosable(true)
    }

    override fun showYearPeriods(periods: Array<Int>) {
        StatisticsPeriodOptionsFragment.newInstance(
                statisticsType = StatisticsType.Year,
                onItemClick = { year -> presenter.chooseYear(year) }
        ).apply {
            values = periods
        }.also {
            it.show(supportFragmentManager)
        }
    }

    override fun showMonthPeriods(periods: Array<Int>) {
        StatisticsPeriodOptionsFragment.newInstance(
                statisticsType = StatisticsType.Month,
                onItemClick = { month -> presenter.chooseMonth(month) }
        ).apply {
            values = periods
        }.also {
            it.show(supportFragmentManager)
        }
    }

    override fun enableNext(isEnabled: Boolean) {
        if (isEnabled) {
            nextTextView.enable()
        } else {
            nextTextView.disable()
        }
    }

    override fun enablePrev(isEnabled: Boolean) {
        if (isEnabled) {
            prevTextView.enable()
        } else {
            prevTextView.disable()
        }
    }

    override fun showProgress() {
        chart.setGone(true)
        progressBar.setGone(false)
    }

    override fun hideProgress() {
        progressBar.setGone(true)
    }

    companion object {
        const val PAGE_PATH = "PAGE_PATH"
    }
}
