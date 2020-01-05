package org.wordpress.aztec.handlers

import org.wordpress.aztec.AztecText
import org.wordpress.aztec.spans.AztecPreformatSpan

class PreformatHandler(aztecText: AztecText) : GenericBlockHandler<AztecPreformatSpan>(AztecPreformatSpan::class.java, aztecText)
