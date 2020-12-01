package com.telex.base.review

import android.app.Activity
import io.reactivex.Completable

interface AppReviewManager {
    fun tryRequestAppReview(activity: Activity): Completable
    fun trackAuthorizedAppLaunch()
}