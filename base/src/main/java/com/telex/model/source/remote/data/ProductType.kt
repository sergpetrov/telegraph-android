package com.telex.model.source.remote.data

import androidx.annotation.StringRes
import com.telex.R

/**
 * @author Sergey Petrov
 */
enum class ProductType(
    @StringRes val title: Int,
    @StringRes val subTitle: Int,
    val sku: String
) {
    Coffee(
            title = R.string.donate_item1_title,
            subTitle = R.string.donate_item1_subtitle,
            sku = "coffee"
    ),
    Smoothie(
            title = R.string.donate_item2_title,
            subTitle = R.string.donate_item2_subtitle,
            sku = "smoothie"
    ),
    Pizza(
            title = R.string.donate_item3_title,
            subTitle = R.string.donate_item3_subtitle,
            sku = "pizza"
    ),
    Meal(
            title = R.string.donate_item4_title,
            subTitle = R.string.donate_item4_subtitle,
            sku = "meal"
    )
}
