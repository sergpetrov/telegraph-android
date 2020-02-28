package com.telex.base.model.source.local

/**
 * @author Sergey Petrov
 */
data class PagedData<out T>(val total: Int, val items: List<T>)
