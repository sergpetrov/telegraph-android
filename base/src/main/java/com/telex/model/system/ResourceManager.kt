package ru.marussia.app.model.system

import android.content.Context
import javax.inject.Inject

/**
 * @author Sergey Petrov
 */
class ResourceManager @Inject constructor(private val context: Context) {

    fun getString(id: Int) = context.getString(id)
}
