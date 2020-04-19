package com.telex.base.presentation.statistics

import android.app.Dialog
import android.os.Bundle
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.view.ContextThemeWrapper
import com.telex.base.R
import com.telex.base.presentation.base.BaseBottomSheetFragment
import com.telex.base.utils.DateUtils

/**
 * @author Sergey Petrov
 */
class StatisticsPeriodOptionsFragment : BaseBottomSheetFragment() {

    override val layout: Int = R.layout.fragment_bottom_sheet_options

    private val statisticsType: StatisticsType by lazy {
        arguments?.getSerializable(STATISTICS_TYPE) as? StatisticsType
                ?: throw IllegalArgumentException("statisticsType can't be null")
    }

    private val title: Int
        get() = when (statisticsType) {
            StatisticsType.Month -> R.string.choose_month
            StatisticsType.Year -> R.string.choose_year
        }

    private lateinit var onItemClick: (Int) -> Unit

    var values = arrayOf<Int>()

    override fun setupView(dialog: Dialog) {
        dialog.findViewById<TextView>(R.id.titleTextView).text = getString(title)

        val options = values.map { value ->
            val title = when (statisticsType) {
                StatisticsType.Month -> DateUtils.getMonthName(context, value, short = false)
                StatisticsType.Year -> value.toString()
            }

            Option(value, title, onClick = onItemClick)
        }

        options.forEach { option -> addOption(option) }
    }

    protected fun addOption(option: Option) {
        val textView = TextView(ContextThemeWrapper(context, R.style.OptionTextViewStyle))
        textView.text = option.title

        textView.setOnClickListener {
            option.onClick.invoke(option.value)
            dismiss()
        }

        dialog?.findViewById<ViewGroup>(R.id.containerLayout)?.addView(textView)
    }

    class Option(val value: Int, val title: String, var onClick: ((Int) -> Unit))

    companion object {
        private const val STATISTICS_TYPE = "STATISTICS_TYPE"

        fun newInstance(
            statisticsType: StatisticsType,
            onItemClick: ((Int) -> Unit)
        ) = StatisticsPeriodOptionsFragment().apply {
            this.onItemClick = onItemClick
            arguments = Bundle().apply {
                putSerializable(STATISTICS_TYPE, statisticsType)
            }
        }
    }
}
