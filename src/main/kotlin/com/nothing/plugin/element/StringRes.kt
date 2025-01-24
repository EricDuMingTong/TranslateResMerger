package com.nothing.plugin.element

/**
 * 表示一个字符串定义
 * <string name="change_grid">Switch layout</string>
 */
class StringRes {
    var prefix: String? = null
    var suffix: String? = null
    var comment: String? = null
    var key: String? = null
    var value: String? = null
    override fun toString(): String {
        return (if (null == comment) "" else comment) + regularPrefix(prefix) + key + INFIX + value + suffix
    }

    // TODO 存在多行的时候，不能很好的格式化
    private fun regularPrefix(prefix: String?): String? {
        if (null == prefix || prefix.isEmpty() || prefix.isBlank()) {
            return prefix
        }
        // Trim start
        val prefixCharArray = prefix.toCharArray()
        for (i in prefixCharArray.indices) {
            if (prefixCharArray[i] != ' ') {
                return ONE_TAB + prefix.substring(i)
            }
        }
        return prefix
    }

    companion object {
        const val ONE_TAB = "    "
        const val INFIX = ">"
    }
}