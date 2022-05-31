package org.sorapointa.utils

import org.w3c.dom.Document
import org.w3c.dom.Node
import org.w3c.dom.NodeList
import javax.xml.parsers.DocumentBuilder
import javax.xml.parsers.DocumentBuilderFactory

fun String.readToXMLDocument(): Document {
    val dbf: DocumentBuilderFactory = DocumentBuilderFactory.newInstance()
    val db: DocumentBuilder = dbf.newDocumentBuilder()
    return db.parse(this.byteInputStream())
}

fun List<Node>.getTextByName(name: String) =
    this.firstOrNull { it.nodeName == name }?.textContent

fun Document.byTagFirst(tag: String): Node? = getElementsByTagName(tag).item(0)

fun NodeList.toList(): List<Node> =
    buildList(length) {
        for (idx in 0 until length) {
            add(item(idx))
        }
    }
