package com.nothing.plugin.element

import java.util.*

object ElementConstants {
    const val STRING_PREFIX = "<string "
    const val STRING_SUFFIX = "</string>"
    const val ARRAY_PREFIX = "<array "
    const val ARRAY_SUFFIX = "</array>"
    const val STRING_ARRAY_PREFIX = "<string-array "
    const val STRING_ARRAY_SUFFIX = "</string-array>"
    const val PLURALS_PREFIX = "<plurals "
    const val PLURALS_SUFFIX = "</plurals>"
    val RES_TAG_ARRAY_LIST = ArrayList(
        Arrays.asList(
            ResTag(STRING_PREFIX, STRING_SUFFIX),
            ResTag(ARRAY_PREFIX, ARRAY_SUFFIX),
            ResTag(STRING_ARRAY_PREFIX, STRING_ARRAY_SUFFIX),
            ResTag(PLURALS_PREFIX, PLURALS_SUFFIX)
        )
    )
}