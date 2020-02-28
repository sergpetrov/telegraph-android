package com.telex.base.presentation.page.format

import com.telex.base.model.system.ServerManager

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
