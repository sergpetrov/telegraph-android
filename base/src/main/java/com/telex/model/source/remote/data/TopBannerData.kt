package com.telex.model.source.remote.data

/**
 * @author Sergey Petrov
 */
data class TopBannerData(
    val disabled: Boolean,
    val showForPro: Boolean,
    val message: String,
    val firstAction: ActionData?,
    val secondAction: ActionData?
) {
    data class ActionData(
        val title: String,
        val url: String?
    )
}
