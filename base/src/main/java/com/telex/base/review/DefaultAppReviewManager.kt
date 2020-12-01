package com.telex.base.review

import android.app.Activity
import io.reactivex.Completable

class DefaultAppReviewManager : AppReviewManager {

    override fun tryRequestAppReview(activity: Activity): Completable {
        return Completable.complete()
    }

    override fun trackAuthorizedAppLaunch() {
    }
}