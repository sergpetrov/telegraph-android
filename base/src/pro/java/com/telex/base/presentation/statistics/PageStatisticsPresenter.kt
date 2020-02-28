package com.telex.base.presentation.statistics

import com.telex.base.extention.toTypedArray
import com.telex.base.model.interactors.PageInteractor
import com.telex.base.model.interactors.PageViewsInteractor
import com.telex.base.model.source.local.entity.Page
import com.telex.base.presentation.base.BasePresenter
import com.telex.base.presentation.base.ErrorHandler
import com.telex.base.utils.DateUtils
import io.reactivex.disposables.Disposable
import javax.inject.Inject
import moxy.InjectViewState

/**
 * @author Sergey Petrov
 */
@InjectViewState
class PageStatisticsPresenter @Inject constructor(
    private val pageInteractor: PageInteractor,
    private val pageViewsInteractor: PageViewsInteractor,
    errorHandler: ErrorHandler
) : BasePresenter<PageStatisticsView>(errorHandler) {

    lateinit var pagePath: String
    private lateinit var page: Page

    var year = DateUtils.getCurrentYear()
    var month = DateUtils.getCurrentMonth() + 1

    private lateinit var statisticsType: StatisticsType
    private val maxYear = DateUtils.getCurrentYear()
    private val minYear = 2016
    private val maxMonth = DateUtils.getCurrentMonth() + 1

    private var disposable: Disposable? = null

    override fun onFirstViewAttach() {
        super.onFirstViewAttach()

        changeStatisticsType(StatisticsType.Month)

        pageInteractor.getCachedPage(pagePath)
                .compositeSubscribe(
                        onSuccess = { page ->
                            this.page = page
                            viewState.showPageTotalViews(page.views)
                            loadPageViews()
                        }
                )
    }

    fun loadPageViews() {
        when (statisticsType) {
            StatisticsType.Month -> loadPageViewsForMonthGroupedByDay(year, month)
            StatisticsType.Year -> loadPageViewsForYearGroupedByMonth(year)
        }
    }

    fun changeStatisticsType(type: StatisticsType) {
        statisticsType = type

        when (statisticsType) {
            StatisticsType.Month -> viewState.updateForMonthStatisticsType()
            StatisticsType.Year -> viewState.updateForYearStatisticsType()
        }
    }

    fun loadPageViewsForPrevPeriod() {
        when (statisticsType) {
            StatisticsType.Month -> loadPageViewsForPrevMonthGroupedByDay()
            StatisticsType.Year -> loadPageViewsForPrevYearGroupedByMonth()
        }
    }

    fun loadPageViewsForNextPeriod() {
        when (statisticsType) {
            StatisticsType.Month -> loadPageViewsForNextMonthGroupedByDay()
            StatisticsType.Year -> loadPageViewsForNextYearGroupedByMonth()
        }
    }

    private fun loadPageViewsForNextMonthGroupedByDay() {
        if (month == 12) {
            month = 1
            year++
        } else {
            month++
        }

        loadPageViewsForMonthGroupedByDay(year, month)
    }

    private fun loadPageViewsForPrevMonthGroupedByDay() {
        if (month == 1) {
            month = 12
            year--
        } else {
            month--
        }

        loadPageViewsForMonthGroupedByDay(year, month)
    }

    private fun loadPageViewsForNextYearGroupedByMonth() {
        year++

        loadPageViewsForYearGroupedByMonth(year)
    }

    private fun loadPageViewsForPrevYearGroupedByMonth() {
        year--

        loadPageViewsForYearGroupedByMonth(year)
    }

    fun loadPageViewsForMonthGroupedByDay(year: Int, month: Int) {
        viewState.enableNext(!(year == maxYear && month == maxMonth))
        viewState.enablePrev(year != minYear)

        viewState.showYear(year)
        viewState.showMonth(month)

        pageViewsInteractor.getPageTotalViewsForMonth(pagePath, year, month)
                .compositeSubscribe(
                        onSuccess = { value ->
                            viewState.showPageViews(value)
                        }
                )

        disposable?.dispose()
        disposable = pageViewsInteractor.getPageViewsForMonthGroupedByDay(pagePath, year, month)
                .doOnSubscribe { viewState.showProgress() }
                .doAfterTerminate { viewState.hideProgress() }
                .compositeSubscribe(
                        onNext = { values -> viewState.showPageViewsSet(values) }
                )
    }

    fun loadPageViewsForYearGroupedByMonth(year: Int) {
        viewState.enableNext(year != maxYear)
        viewState.enablePrev(year != minYear)

        viewState.showYear(year)

        pageViewsInteractor.getPageTotalViewsForYear(pagePath, year)
                .compositeSubscribe(
                        onSuccess = { value ->
                            viewState.showPageViews(value)
                        }
                )

        disposable?.dispose()
        disposable = pageViewsInteractor.getPageViewsForYearGroupedByMonth(pagePath, year)
                .doOnSubscribe { viewState.showProgress() }
                .doAfterTerminate { viewState.hideProgress() }
                .compositeSubscribe(
                        onNext = { values -> viewState.showPageViewsSet(values) }
                )
    }

    fun onYearClicked() {
        viewState.showYearPeriods((maxYear downTo minYear).toTypedArray())
    }

    fun onMonthClicked() {
        val maxValue = if (year == maxYear) maxMonth else DateUtils.MONTHS_IN_YEAR
        viewState.showMonthPeriods((1..maxValue).toTypedArray())
    }

    fun chooseYear(year: Int) {
        this.year = year

        if (year == maxYear && month > maxMonth) {
            changeStatisticsType(StatisticsType.Year)
        }

        loadPageViews()
    }

    fun chooseMonth(month: Int) {
        this.month = month
        changeStatisticsType(StatisticsType.Month)
        loadPageViews()
    }
}
