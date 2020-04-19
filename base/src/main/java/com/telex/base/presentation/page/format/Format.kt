package com.telex.base.presentation.page.format

import java.util.UUID

/**
 * @author Sergey Petrov
 */
open class Format(
    open var type: FormatType,
    open var html: String = getEmptyHtml(type).orEmpty()
) {
    val id = UUID.nameUUIDFromBytes((type.tag + html).toByteArray()).toString()

    open fun toHtml() = html

    fun putHtml(html: String) {
        this.html = html
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Format) return false

        if (type != other.type) return false
        if (html != other.html) return false
        if (id != other.id) return false

        return true
    }

    override fun hashCode(): Int {
        var result = type.hashCode()
        result = 31 * result + html.hashCode()
        result = 31 * result + id.hashCode()
        return result
    }

    companion object {

        fun getEmptyHtml(type: FormatType): String? {
            return when (type) {
                FormatType.PARAGRAPH -> "<p><br></p>"
                FormatType.QUOTE -> "<blockquote></blockquote>"
                FormatType.HORIZONTAL_RULE -> "<hr>"
                FormatType.HEADING -> "<h3></h3>"
                FormatType.SUB_HEADING -> "<h4></h4>"
                FormatType.UNORDERED_LIST -> "<ul><li></li></ul>"
                FormatType.ORDERED_LIST -> "<ol><li></li></ol>"
                else -> null
            }
        }
    }
}
