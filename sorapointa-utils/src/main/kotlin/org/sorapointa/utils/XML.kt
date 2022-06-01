package org.sorapointa.utils

import org.w3c.dom.Document
import org.w3c.dom.Node
import org.w3c.dom.NodeList
import javax.xml.parsers.DocumentBuilder
import javax.xml.parsers.DocumentBuilderFactory

/**
 * @receiver [String] receive a string, read to XML [Document]
 * @return [Document] XML Parsed Document
 */
fun String.readToXMLDocument(): Document {
    val dbf: DocumentBuilderFactory = DocumentBuilderFactory.newInstance()
    val db: DocumentBuilder = dbf.newDocumentBuilder()
    return db.parse(this.byteInputStream())
}

/**
 * @param name name of [Node]
 * @receiver list of [Node]
 * @return **nullable** [String], the text content from specified [Node]
 */
fun List<Node>.getTextByName(name: String): String? =
    this.firstOrNull { it.nodeName == name }?.textContent

/**
 * @param tag name of tag
 * @receiver XML [Document]
 * @return **nullable** [Node] get the first [Node] with specified name from XML [Document]
 */
fun Document.byTagFirst(tag: String): Node? = getElementsByTagName(tag).item(0)

/**
 * @receiver [NodeList] from XML [Document]
 * @return [List] of [Node]
 */
fun NodeList.toList(): List<Node> =
    buildList(length) {
        for (idx in 0 until length) {
            add(item(idx))
        }
    }
