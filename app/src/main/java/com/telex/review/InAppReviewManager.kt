package com.telex.review

import android.app.Activity
import android.content.Context
import com.google.android.play.core.review.ReviewManagerFactory
import com.telex.base.analytics.AnalyticsHelper
import com.telex.base.model.source.local.AppData
import com.telex.base.review.AppReviewManager
import io.reactivex.Completable
import java.lang.ref.WeakReference
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

/**
 * @author Sergey Petrov
 */
private val APP_REVIEW_REQUEST_INTERVAL = TimeUnit.DAYS.toMillis(60) // 2 months

@Singleton
class InAppReviewManager constructor(
        private val context: Context,
        private val appData: AppData
) : AppReviewManager {

    override fun tryRequestAppReview(activity: Activity): Completable {
        val activityReference = WeakReference(activity)
        return Completable.create { emitter ->
            if (needRequestAppReview()) {
                val reviewManager = ReviewManagerFactory.create(context)
                val requestReviewFlow = reviewManager.requestReviewFlow()

                AnalyticsHelper.logAppReviewRequested()
                appData.putLastAppReviewRequestTime(System.currentTimeMillis())

                requestReviewFlow.addOnCompleteListener { request ->
                    when {
                        request.isSuccessful -> {
                            activityReference.get()?.apply {
                                reviewManager.launchReviewFlow(this, request.result)
                                        .addOnCompleteListener {
                                            emitter.onComplete()
                                        }.addOnFailureListener { emitter.tryOnError(it) }
                            } ?: emitter.onComplete()
                        }
                        else -> {
                            emitter.onComplete()
                        }
                    }
                }.addOnFailureListener { emitter.tryOnError(it) }
            } else emitter.onComplete()
        }
    }

    override fun trackAuthorizedAppLaunch() {
        appData.putAuthorizedAppLaunch(appData.getAuthorizedAppLaunch() + 1)
    }

    private fun needRequestAppReview(): Boolean {
        return appData.getAuthorizedAppLaunch() % 10 == 0 &&
                System.currentTimeMillis() - appData.getLastAppReviewRequestTime() > APP_REVIEW_REQUEST_INTERVAL
    }
}
