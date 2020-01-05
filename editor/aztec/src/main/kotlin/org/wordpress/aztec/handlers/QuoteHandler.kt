package org.wordpress.aztec.handlers

import org.wordpress.aztec.AztecText
import org.wordpress.aztec.spans.AztecQuoteSpan

class QuoteHandler(aztecText: AztecText) : GenericBlockHandler<AztecQuoteSpan>(AztecQuoteSpan::class.java, aztecText)