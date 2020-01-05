package org.wordpress.aztec.handlers

import org.wordpress.aztec.AztecText
import org.wordpress.aztec.spans.AztecListSpan
import org.wordpress.aztec.watchers.TextDeleter

class ListHandler(aztecText: AztecText) : GenericBlockHandler<AztecListSpan>(AztecListSpan::class.java, aztecText) {

    override fun shouldHandle(): Boolean {
        return block.span.nestingLevel in (nestingLevel - 1)..(nestingLevel)
    }

    override fun handleNewlineAtEmptyLineAtBlockEnd() {
        super.handleNewlineAtEmptyLineAtBlockEnd()

        TextDeleter.mark(text, newlineIndex - 1, newlineIndex)
    }
}