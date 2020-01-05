package com.telex.presentation.page.format

import com.telex.model.system.ServerManager

/**
 * @author Sergey Petrov
 */
data class VideoFormat(
    override val childHtml: String,
    override val src: String,
    override var caption: String,
    override var type: FormatType = FormatType.VIDEO
) : MediaFormat(childHtml, src, caption, type) {

    override fun getUrl(): String {
        return ServerManager.endPoint + src
    }
}
