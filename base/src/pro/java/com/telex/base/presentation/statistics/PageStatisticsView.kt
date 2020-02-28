package com.telex.base.presentation.statistics

import com.telex.base.presentation.base.BaseMvpView
import moxy.viewstate.strategy.AddToEndSingleStrategy
import moxy.viewstate.strategy.OneExecutionStateStrategy
import moxy.viewstate.strategy.StateStrategyType

/**
 * @author Sergey Petrov
 */
@StateStrategyType(AddToEndSingleStrategy::class)
interface PageStatisticsView : BaseMvpView {
    fun showPageViewsSet(values: Map<Int, Int>)
    fun showPageViews(viewsCount: Int)
    fun showPageTotalViews(viewsCount: Int)
    @StateStrategyType(OneExecutionStateStrategy::class)
    fun showProgress()
    fun hideProgress()
    fun enableNext(isEnabled: Boolean)
    fun enablePrev(isEnabled: Boolean)
    fun updateForMonthStatisticsType()
    fun updateForYearStatisticsType()
    @StateStrategyType(OneExecutionStateStrategy::class)
    fun showYear(year: Int)
    @StateStrategyType(OneExecutionStateStrategy::class)
    fun showMonth(month: Int)
    @StateStrategyType(OneExecutionStateStrategy::class)
    fun showYearPeriods(periods: Array<Int>)
    @StateStrategyType(OneExecutionStateStrategy::class)
    fun showMonthPeriods(periods: Array<Int>)
}
