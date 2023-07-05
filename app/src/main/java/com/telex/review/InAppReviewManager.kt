package com.telex.review

import android.app.Activity
import android.content.Context
import com.google.android.play.core.review.ReviewManagerFactory
import com.telex.base.analytics.AnalyticsHelper
import com.telex.base.model.source.local.AppData
import com.telex.base.review.AppReviewManager
import io.reactivex.Completable
import java.time.Duration
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

private val APP_REVIEW_REQUEST_INTERVAL = Duration.ofDays(60).toMillis()

@Singleton
class InAppReviewManager constructor(
        private val context: Context,
        private val appData: AppData
) : AppReviewManager {

    override fun tryRequestAppReview(activity: Activity): Completable {
        return Completable.create { emitter ->
            appData.incrementAuthorizedAppLaunchCount()

            if (needRequestAppReview()) {
                val reviewManager = ReviewManagerFactory.create(context)
                val requestReviewFlow = reviewManager.requestReviewFlow()

                AnalyticsHelper.logAppReviewRequested()
                appData.putLastAppReviewRequestTime(System.currentTimeMillis())

                requestReviewFlow.addOnCompleteListener { request ->
                    activity.let {
                        if (request.isSuccessful) {
                            reviewManager.launchReviewFlow(it, request.result)
                                .addOnCompleteListener { emitter.onComplete() }
                                .addOnFailureListener { emitter.tryOnError(it) }
                        } else {
                            emitter.onComplete()
                        }
                    }
                }.addOnFailureListener { emitter.tryOnError(it) }
            } else {
                emitter.onComplete()
            }
        }
    }

    private fun needRequestAppReview(): Boolean {
        val hasEnoughAppLaunches = appData.getAuthorizedAppLaunch() % 10 == 0
        val enoughTimeHasPassed = System.currentTimeMillis() - appData.getLastAppReviewRequestTime() > APP_REVIEW_REQUEST_INTERVAL
        return hasEnoughAppLaunches && enoughTimeHasPassed
    }
}

private fun AppData.incrementAuthorizedAppLaunchCount() {
    putAuthorizedAppLaunch(getAuthorizedAppLaunch() + 1)
}
