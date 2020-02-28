package com.telex.base.presentation.page.format

import com.telex.base.extention.isUrl
import com.telex.base.model.system.ServerManager
import com.telex.base.presentation.page.adapter.ImageUploadStatus

/**
 * @author Sergey Petrov
 */
data class ImageFormat(
    var url: String,
    var caption: String
) : Format(FormatType.IMAGE, "<figure><img src=\"$url\"/><figcaption>$caption</figcaption></figure>") {

    var uploadStatus: ImageUploadStatus? = null

    override fun toHtml(): String {
        return "<figure><img src=\"$url\"/><figcaption>$caption</figcaption></figure>"
    }

    fun getFullUrl() = if (!url.isUrl()) ServerManager.endPoint + url else url
}
