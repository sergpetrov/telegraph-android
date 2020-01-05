package ru.marussia.app.model.system

import io.reactivex.Scheduler

/**
 * @author Sergey Petrov
 */
interface SchedulersProvider {
    fun ui(): Scheduler
    fun computation(): Scheduler
    fun trampoline(): Scheduler
    fun newThread(): Scheduler
    fun io(): Scheduler
}
