/*
package com.nothing.translatemerge.file

import java.io.File
import java.io.FileOutputStream
import java.io.OutputStreamWriter
import java.nio.charset.StandardCharsets

import org.dom4j.Attribute
import org.dom4j.Document
import org.dom4j.Element
import org.dom4j.io.SAXReader
import org.dom4j.io.XMLWriter


class StringsXmlFile(folder: String, val name: String) {
    private val mDocument: Document?

    init {
        mDocument = read("$folder/$name")
    }

    val path: String
        get() = mDocument.getName()

    */
/**
     * @return Represent all strings define in <resources> strings </resources>
     *//*

    val resourcesElement: Element
        get() = mDocument.getRootElement()

    fun write(fileName: String?): Boolean {
        var success = true
        try {
            val writer = XMLWriter(OutputStreamWriter(FileOutputStream(fileName), StandardCharsets.UTF_8))
            writer.write(mDocument)
            writer.close()
        } catch (e: Exception) {
            success = false
            e.printStackTrace()
        }
        return success
    }

    private fun read(filePath: String): Document? {
        var document: Document? = null
        try {
            val reader = SAXReader()
            document = reader.read(File(filePath))
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return document
    }

    override fun toString(): String {
        val strBuilder = StringBuilder()
        // <resources>resourcesElement</resources>
        val resourcesElement: Element = mDocument.getRootElement()
        strBuilder.append(resourcesElement.getName()).append("\n")
        val stringIterator: Iterator<Element> = resourcesElement.elementIterator()
        while (stringIterator.hasNext()) {
            // <string></string>
            val oneString: Element = stringIterator.next()
            val stringAttrs: List<Attribute> = oneString.attributes()
            strBuilder.append("<").append(oneString.getName()).append(" ")
            var count = 1
            for (attr in stringAttrs) {
                strBuilder.append(attr.getName()).append("=").append(attr.getStringValue())
                count++
                if (count > 1) {
                    strBuilder.append(" ")
                }
            }
            */
/**
             * Iterator<Element> stringArray = oneString.elementIterator();
             * while (stringArray.hasNext()) {
             * Element item = stringArray.next();
             * System.out.println("Item is: <" + item.getName() + ">" + item.getStringValue() + "</Element>" + item.getName() + ">");
             * }
             *//*

            strBuilder.append(">").append(oneString.getStringValue()).append("</").append(oneString.getName())
                .append(">\n")
        }
        return strBuilder.toString()
    }
}*/
