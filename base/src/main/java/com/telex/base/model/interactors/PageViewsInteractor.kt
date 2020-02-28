package com.telex.base.model.interactors

import com.telex.base.extention.withDefaults
import com.telex.base.model.repository.PageViewsRepository
import com.telex.base.utils.DateUtils
import io.reactivex.Observable
import io.reactivex.Single
import javax.inject.Inject

/**
 * @author Sergey Petrov
 */
class PageViewsInteractor @Inject constructor(
    private val pageViewsRepository: PageViewsRepository
) {

    fun getPageTotalViewsForYear(path: String, year: Int): Single<Int> {
        return pageViewsRepository.getPageViewsForYear(path, year)
                .withDefaults()
    }

    fun getPageTotalViewsForMonth(path: String, year: Int, month: Int): Single<Int> {
        return pageViewsRepository.getPageViewsForMonth(path, year, month)
                .withDefaults()
    }

    fun getPageViewsForMonthGroupedByDay(path: String, year: Int, month: Int): Observable<Map<Int, Int>> {
        val dayInMonth = DateUtils.getDaysInMonth(year, month)
        val days = 1..dayInMonth

        val dayObservables = days.map { day ->
            pageViewsRepository.getPageViewsForDay(path, year, month, day).toObservable()
        }

        return Observable
                .zip(dayObservables) { args ->
                    args.mapIndexed { day, views -> day + 1 to views as Int }.toMap()
                }
                .withDefaults()
    }

    fun getPageViewsForYearGroupedByMonth(path: String, year: Int): Observable<Map<Int, Int>> {
        val months = 1..DateUtils.MONTHS_IN_YEAR

        val monthsObservables = months.map { month ->
            pageViewsRepository.getPageViewsForMonth(path, year, month).toObservable()
        }

        return Observable
                .zip(monthsObservables) { args ->
                    args.mapIndexed { month, views -> month to views as Int }.toMap()
                }
                .withDefaults()
    }
}
