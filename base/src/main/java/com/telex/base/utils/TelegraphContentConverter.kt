package com.telex.base.utils

import com.telex.base.model.source.remote.data.NodeElementData
import javax.inject.Inject
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import org.jsoup.nodes.Node
import org.jsoup.nodes.TextNode
import org.jsoup.safety.Whitelist

/**
 * @author Sergey Petrov
 */
class TelegraphContentConverter @Inject constructor() {

    private val allowedTags = Whitelist()
            .addTags(
                    "a", "aside", "b", "blockquote", "br",
                    "code", "em", "figcaption", "figure", "h3", "h4",
                    "hr", "i", "iframe", "img", "li", "ol", "p",
                    "pre", "s", "strong", "u", "ul", "video"
            )
            .addAttributes("h3", "id")
            .addAttributes("h4", "id")
            .addAttributes("a", "href", "target")
            .addAttributes("img", "src", "width", "height")
            .addAttributes("video", "src", "controls", "preload", "autoplay", "loop", "muted", "width", "height")
            .addAttributes("iframe", "src", "scrolling", "allowfullscreen", "width", "height", "allowtransparency", "frameborder")

    fun nodesToHtml(content: List<NodeElementData>): String {
        val document = Jsoup.parseBodyFragment("")
        document.outputSettings().prettyPrint(false)
        for (i in content.indices) {
            val node = content[i]
            nodeToDom(document, document.body(), node, i)
        }
        return document.body().html()
    }

    fun htmlToNodes(html: String): ArrayList<NodeElementData> {
        val content = ArrayList<NodeElementData>()
        val normalizedHtml = normalizeHtml(html)
        val normalizedDocument = Jsoup.parseBodyFragment(normalizedHtml)
        normalizedDocument.outputSettings().prettyPrint(false)
        for (node in normalizedDocument.body().childNodes()) {
            val nodeElementData = domAnyToNode(node)
            nodeElementData?.let {
                content.add(it)
            }
        }
        return content
    }

    private fun normalizeHtml(html: String): String {
        val document = Jsoup.parseBodyFragment(html)
        document.outputSettings().prettyPrint(false)

        document.select("h1").tagName("h3")
        document.select("h2").tagName("h3")
        document.select("h5").tagName("h4")
        document.select("h6").tagName("h4")
        document.select("i").tagName("em")

        wrapInFigureIfNeeded(document, "img")
        wrapInFigureIfNeeded(document, "video")
        wrapInFigureIfNeeded(document, "iframe")

        val figures = document.select("figure")
        for (f in figures) {
            val invalid = f.children().none { it.tagName() == "img" || it.tagName() == "iframe" || it.tagName() == "video" }
            if (invalid) {
                f.remove()
            } else {
                if (f.children().none { it.tagName() == "figcaption" }) {
                    f.appendElement("figcaption")
                }

                f.children().forEach {
                    if (it.tagName() != "img" && it.tagName() != "iframe" && it.tagName() != "video" && it.tagName() != "figcaption") {
                        it.remove()
                    }
                }
            }
        }

        val figcaptions = document.select("figcaption")
        figcaptions
                .filter { it.children() == null || it.children().isEmpty() }
                .forEach { it.appendText("") }

        val result = Jsoup
                .clean(document.html(), "", allowedTags, document.outputSettings())
                .replace("&nbsp;", " ")

        return result
    }

    private fun wrapInFigureIfNeeded(document: Document, tag: String) {
        val elements = document.select(tag)
        for (i in elements) {
            val invalid = i.parents().none { it.tagName() == "figure" }
            if (invalid) {
                i.wrap("<figure></figure>")
            }
        }
    }

    private fun nodeToDom(document: Document, domNode: Element, node: NodeElementData, index: Int): Element? {
        if (!node.text.isNullOrEmpty()) {
            return domNode.appendChild(TextNode(node.text))
        } else {
            val newDomNode: Element
            if (node.tag != null) {
                newDomNode = document.createElement(node.tag)
                node.attrs?.let {
                    it.forEach { (name, value) ->
                        newDomNode.attr(name, value)
                    }
                }

                node.children?.let {
                    for (child in it) {
                        nodeToDom(document, newDomNode, child, index)
                    }
                }
                domNode.appendChild(newDomNode)
            }
        }
        return null
    }

    private fun domToNode(domNode: Node): NodeElementData? {
        if (domNode is Element) {
            val nodeElement = NodeElementData(tag = domNode.tagName().toLowerCase())
            if (domNode.attributes() != null && domNode.attributes().size() > 0) {
                if (nodeElement.attrs == null) {
                    nodeElement.attrs = mutableMapOf()
                }
                for (attr in domNode.attributes()) {
                    nodeElement.attrs?.set(attr.key, attr.value ?: "")
                    if (attr.key == "muted") { // it's needed because jsoup removes `muted` value for this attr for video tag
                        nodeElement.attrs?.set(attr.key, attr.value ?: "muted")
                    }
                }
            }
            if (domNode.childNodes() != null && domNode.childNodes().isNotEmpty()) {
                if (nodeElement.children == null) {
                    nodeElement.children = arrayListOf()
                }
                for (child in domNode.childNodes()) {
                    domAnyToNode(child)?.let { nodeElement.children?.add(it) }
                }
            }
            return nodeElement
        }
        return null
    }

    private fun domAnyToNode(domNode: Node): NodeElementData? {
        if (domNode is TextNode) {
            if (!domNode.wholeText.isNullOrEmpty()) {
                return NodeElementData(text = domNode.wholeText)
            }
        } else {
            return domToNode(domNode)
        }
        return null
    }
}
