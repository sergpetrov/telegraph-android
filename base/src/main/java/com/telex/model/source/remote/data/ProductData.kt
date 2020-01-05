package com.telex.model.source.remote.data

/**
 * @author Sergey Petrov
 */
data class ProductData(
    val type: ProductType,
    val price: String,
    var hasPurchase: Boolean = false
)
