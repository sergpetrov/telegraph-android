package com.telex.presentation.page.format

import org.wordpress.aztec.AztecTextFormat
import org.wordpress.aztec.ITextFormat

enum class FormatType(val tag: String, val aztecFormat: AztecTextFormat? = null) {
    BREAK_LINE("br", null),
    PARAGRAPH("p", AztecTextFormat.FORMAT_PARAGRAPH),
    QUOTE("blockquote", AztecTextFormat.FORMAT_QUOTE),
    ASIDE("aside", AztecTextFormat.FORMAT_ASIDE),
    HORIZONTAL_RULE("hr", AztecTextFormat.FORMAT_HORIZONTAL_RULE),
    HEADING("h3", AztecTextFormat.FORMAT_HEADING_3),
    SUB_HEADING("h4", AztecTextFormat.FORMAT_HEADING_4),
    BOLD("b", AztecTextFormat.FORMAT_BOLD),
    STRONG("strong", AztecTextFormat.FORMAT_STRONG),
    ITALIC("em", AztecTextFormat.FORMAT_ITALIC),
    UNDERLINE("u", AztecTextFormat.FORMAT_UNDERLINE),
    STRIKETHROUGH("s", AztecTextFormat.FORMAT_STRIKETHROUGH),
    LINK("a", AztecTextFormat.FORMAT_LINK),
    UNORDERED_LIST("ul", AztecTextFormat.FORMAT_UNORDERED_LIST),
    ORDERED_LIST("ol", AztecTextFormat.FORMAT_ORDERED_LIST),
    FIGURE("figure", null),
    IMAGE("img", null),
    IFRAME("iframe", null),
    VIDEO("video", null),
    PREFORMAT("pre", AztecTextFormat.FORMAT_PREFORMAT);

    fun isInline(): Boolean {
        return inlineFormats.contains(this)
    }

    companion object {

        val inlineFormats = listOf(BOLD, STRONG, ITALIC, UNDERLINE, STRIKETHROUGH, LINK)

        fun getByOrdinal(ordinal: Int): FormatType? {
            values().forEach { formatType ->
                if (formatType.ordinal == ordinal) {
                    return formatType
                }
            }
            return null
        }

        fun getByTag(tag: String?): FormatType? {
            values().forEach { formatType ->
                if (formatType.tag == tag) {
                    return formatType
                }
            }
            return null
        }

        fun getByAztecFormat(format: ITextFormat?): FormatType? {
            values().forEach { formatType ->
                if (formatType.aztecFormat == format) {
                    return formatType
                }
            }
            return null
        }
    }
}
