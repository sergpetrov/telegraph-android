package com.telex.presentation.page.format

import java.net.URLDecoder

/**
 * @author Sergey Petrov
 */
open class MediaFormat(
    open val childHtml: String,
    open val src: String,
    open var caption: String,
    override var type: FormatType = FormatType.IFRAME
) : Format(type, childHtml) {

    override fun toHtml(): String {
        return "<figure>$childHtml<figcaption>$caption</figcaption></figure>"
    }

    open fun getUrl(): String {
        return URLDecoder.decode(src.substringAfter("?url="), "utf-8")
    }
}
