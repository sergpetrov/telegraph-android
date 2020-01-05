package org.wordpress.aztec.handlers

import org.wordpress.aztec.AztecText
import org.wordpress.aztec.spans.IAztecBlockSpan
import org.wordpress.aztec.watchers.BlockElementWatcher
import org.wordpress.aztec.watchers.TextDeleter
import java.lang.ref.WeakReference

/**
 * A general html block editing handler that closes when a newline is entered at an empty line ("double-enter").
 * If completely empty, the whole block is removed with double-enter.
 */
open class GenericBlockHandler<T : IAztecBlockSpan>(clazz: Class<T>, aztecText: AztecText) : BlockHandler<T>(clazz) {
    // fun handleNewlineAtStartOfBlock()
    // nothing special to do

    protected val aztecTextRef: WeakReference<AztecText?> = WeakReference(aztecText)

    override fun handleNewlineAtEmptyLineAtBlockEnd() {
        // adjust the block end to only include the chars before the newline just added
        block.end = newlineIndex

        // delete the newline
        TextDeleter.mark(text, newlineIndex, newlineIndex + 1)

        // re-play the newline so parent blocks can process it now that the current block has retracted before it
        BlockElementWatcher.replay(text, newlineIndex)

        aztecTextRef.get()?.getOnEnterForBlockListener()?.onEnterKey(false)
    }

    override fun handleNewlineAtEmptyBody() {
        // block is empty so, remove it
        block.remove()

        // delete the newline
        TextDeleter.mark(text, newlineIndex, newlineIndex + 1)

        aztecTextRef.get()?.getOnEnterForBlockListener()?.onEnterKey(true)
    }

    // fun handleNewlineAtTextEnd()
    // got a newline while being at the end-of-text. We'll let the block engulf it.

    // fun handleNewlineInBody()
    // newline added at some position inside the block. Nothing special to do.

    // fun handleEndOfBufferMarker()
    // nothing special to do
}
