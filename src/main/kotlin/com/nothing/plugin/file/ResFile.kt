package com.nothing.plugin.file

import com.nothing.plugin.element.ElementConstants
import com.nothing.plugin.element.StringRes
import java.io.*
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Paths

/**
 * 代表一个资源文件
 * 需要提供资源文件读/写功能；读取时需将内容存储在map中便于操作；写入时需将内容规整为资源文件的格式。
 */
// TODO ResFile 应该用 StringXmlFile 替代，以便获得更快的解析速度以及解析准确度
class ResFile {
    var resFilePath: String
        private set
    var resFileName: String? = null
    var fileVersionAndEncodeType: String? = null
        private set
    var resPrefix: String? = null
        private set
    var resSuffix: String? = null
        private set
    private var mStringContents: ArrayList<StringRes>? = null
    private var mIgnoreStringNames: ArrayList<String>? = null
    var stringContents: ArrayList<StringRes>?
        get() = mStringContents
        set(stringContents) {
            mStringContents = stringContents
        }

    constructor(resFilePath: String) {
        this.resFilePath = resFilePath
        decodeFileName(resFilePath)
        decodeFile(resFilePath)
    }

    constructor(resFilePath: String, ignoreStringNames: ArrayList<String>?) {
        this.resFilePath = resFilePath
        mIgnoreStringNames = ignoreStringNames
        decodeFileName(resFilePath)
        decodeFile(resFilePath)
    }

    private fun decodeFileName(filePath: String) {
        resFileName = filePath.substring(filePath.lastIndexOf("/") + 1)
    }

    private fun decodeFile(resFilePath: String) {
        try {
            val inputStream = BufferedInputStream(Files.newInputStream(Paths.get(resFilePath)))
            val tmpReader = InputStreamReader(inputStream, StandardCharsets.UTF_8)
            val reader = BufferedReader(tmpReader)
            var commentBuffer: StringBuffer? = null
            while (true) {
                var line: String = reader.readLine() ?: break
                var tempStr = line
                tempStr = tempStr.trim { it <= ' ' }
                //System.out.println("line: " + line + "\n tempStr: " + tempStr);
                if (tempStr.startsWith("<?xml")) {
                    fileVersionAndEncodeType = line
                    continue
                }
                if (tempStr.startsWith("<resources")) {
                    resPrefix = line
                    continue
                }
                if (tempStr == "</resources>") {
                    resSuffix = line
                    continue
                }
                if (tempStr == "") {
                    if (commentBuffer == null) {
                        commentBuffer = StringBuffer()
                    }
                    commentBuffer.append(line).append(ENTER)
                    continue
                }
                if (tempStr.startsWith("<!--")) {
                    if (commentBuffer == null) {
                        commentBuffer = StringBuffer()
                    }
                    commentBuffer.append(line).append(ENTER)
                    // 不支持在一行中结束符后还有其它内容的情形 ^_^
                    while (!tempStr.endsWith("-->")) {
                        line = reader.readLine()
                        tempStr = line
                        tempStr = tempStr.trim { it <= ' ' }
                        commentBuffer.append(line).append(ENTER)
                    }
                    continue
                }
                if (parseElement(reader, line, commentBuffer)) {
                    commentBuffer = null
                    //continue;
                }
            }
            //System.out.println("Total res strings == " + (null != mStringContents ? mStringContents.size() : 0));
        } catch (e: IOException) {
            throw RuntimeException(e)
        }
    }

    private fun parseVersionAndEncodeType(line: String): Boolean {
        if (line.startsWith("<?xml")) {
            fileVersionAndEncodeType = line
            return true
        }
        return false
    }

    private fun parseComment(): Boolean {
        return false
    }

    private fun parseElement(reader: BufferedReader, line: String, commentBuffer: StringBuffer?): Boolean {
        for (tag in ElementConstants.RES_TAG_ARRAY_LIST) {
            if (traverseContents(reader, line, commentBuffer, tag.prefix, tag.suffix)) {
                return true
            }
        }
        return false
    }

    private fun traverseContents(
        reader: BufferedReader,
        line: String,
        commentBuffer: StringBuffer?,
        prefix: String,
        suffix: String
    ): Boolean {
        var trimStr = line
        trimStr = trimStr.trim { it <= ' ' }
        if (trimStr.startsWith(prefix)) {
            if (null == mStringContents) {
                mStringContents = ArrayList<StringRes>()
            }
            val strRes = StringRes()
            strRes.prefix = line.substring(0, line.indexOf(prefix) + prefix.length)
            strRes.suffix = suffix
            if (commentBuffer != null && commentBuffer.length > 0) {
                strRes.comment = commentBuffer.toString()
            }
            val tmpStr = traverseContentsWithSuffix(reader, line, suffix)
            //System.out.println("tmpStr: " + tmpStr);
            strRes.key = tmpStr.substring(tmpStr.indexOf(prefix) + prefix.length, tmpStr.indexOf(StringRes.INFIX))
            //System.out.println("strRes.name == " + strRes.key);
            // 这里有可能会包含 translatable="false" 这种情况，暂时不做处理，直接当作 key 来处理
            strRes.value = tmpStr.substring(tmpStr.indexOf(StringRes.INFIX) + 1, tmpStr.indexOf(suffix))
            if (needToIgnoreString(strRes.key)) {
                System.out.println("ignored String key: " + strRes.key)
                return true
            }
            mStringContents!!.add(strRes)
            return true
        }
        return false
    }

    private fun needToIgnoreString(stringName: String?): Boolean {
        return stringName != null && null != mIgnoreStringNames && mIgnoreStringNames!!.contains(stringName)
    }

    private fun traverseContentsWithSuffix(reader: BufferedReader, lineStrig: String, suffix: String): String {
        var line = lineStrig
        val buffer = StringBuilder(line)
        var firstLineContent = true
        var tempStr = line
        tempStr = tempStr.trim { it <= ' ' }
        try {
            while (!tempStr.endsWith(suffix)) {
                line = reader.readLine()
                tempStr = line
                tempStr = tempStr.trim { it <= ' ' }
                if (firstLineContent) {
                    buffer.append(ENTER)
                    firstLineContent = false
                }
                buffer.append(line).append(ENTER)
            }
        } catch (e: IOException) {
            throw RuntimeException(e)
        }
        return buffer.toString()
    }

    fun compareAndUpdateOrAdd(destFile: ResFile): ResFile {
        val newAdd: ArrayList<StringRes> = ArrayList<StringRes>()
        for (translate in stringContents!!) {
            var exist = false
            for (dest in destFile.stringContents!!) {
                if (translate.key.equals(dest.key)) {
                    exist = true
                    if (!translate.value.equals(dest.value)) {
                        // 存在 name 相同但字符串不同，证明翻译更新了，替换进去
                        //System.out.println("Update String:\n dest == " + dest + "\n  translate == " + translate);
                        dest.value = translate.value
                    }
                    break
                }
            }
            if (!exist) {
                //System.out.println("New add string: " + translate);
                newAdd.add(translate)
            }
        }
        if (newAdd.size > 0) {
            // 新增的字符串，统一添加到最后
            val oldSize = destFile.stringContents!!.size
            destFile.stringContents!!.addAll(oldSize, newAdd)
        }
        return destFile
    }

    private fun regularNewTranslateRes(newTranslate: StringRes): StringRes {
        return newTranslate
    }

    fun addAStringRes(strRes: StringRes) {
        if (null == mStringContents) {
            mStringContents = ArrayList<StringRes>()
        }
        mStringContents!!.add(strRes)
    }

    fun writeToFile(filePath: String?) {
        FileUtils.createFileIfNotExist(filePath)
        val contents = ArrayList<String>()
        if (fileVersionAndEncodeType != null) {
            contents.add(fileVersionAndEncodeType!!)
        }
        if (resPrefix != null) {
            contents.add(resPrefix!!)
        }
        for (aStr in stringContents!!) {
            contents.add(ENTER + aStr.toString())
        }
        if (resSuffix != null) {
            contents.add(resSuffix!!)
        }
        try {
            val bw = BufferedWriter(
                OutputStreamWriter(
                    Files.newOutputStream(File(filePath).toPath()),
                    StandardCharsets.UTF_8
                )
            )
            var firstLine = true
            for (content in contents) {
                var newContent = content
                if (!firstLine && !content.contains(ENTER)) {
                    newContent = ENTER + content
                }
                bw.write(newContent)
                bw.flush()
                firstLine = false
            }
            bw.close()
        } catch (e: IOException) {
            // TODO Auto-generated catch block
            e.printStackTrace()
        }
    }

    fun isStringResContained(stringName: String): Boolean {
        if (null == stringContents || stringContents!!.size <= 0) {
            return false
        }
        for (str in stringContents!!) {
            if (stringName == str.key) {
                return true
            }
        }
        return false
    }

    fun getContainedStringRes(stringName: String): StringRes? {
        if (null == stringContents || stringContents!!.size <= 0) {
            return null
        }
        for (str in stringContents!!) {
            if (stringName == str.key) {
                if (str.value.isNullOrBlank()) {
                    println(resFilePath + " translate res error: " + str.key + " with empty value!!!!")
                    val empty = StringRes()
                    empty.value = XmlConstants.EMPTY_VALUE
                    return empty
                }
                return str
            }
        }
        return null
    }

    companion object {
        // For linux
        //private static String ENTER = "\n";
        // For windows
        private const val ENTER = "\r\n"
    }
}