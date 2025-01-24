package com.nothing.plugin.file

object XmlConstants {
    var ROOT_ATTR = "resources"
    const val EMPTY_VALUE = "~^_^~"
    fun isValidStringsFileName(fileName: String): Boolean {
        return ((fileName.contains("strings")
                || fileName.contains("nothing_string")
                || fileName.contains("plurals")
                || fileName.contains("arrays"))
                && !fileName.contains("donottranslate"))
    }
}