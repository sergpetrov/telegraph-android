package com.telex.presentation.page.format

import com.telex.extention.isUrl
import com.telex.model.system.ServerManager
import com.telex.presentation.page.adapter.ImageUploadStatus

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
