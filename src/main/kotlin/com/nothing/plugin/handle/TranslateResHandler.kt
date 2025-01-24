package com.nothing.plugin.handle

import com.nothing.plugin.element.StringRes
import com.nothing.plugin.file.FileUtils.writeContentToFile
import com.nothing.plugin.file.ResFile
import com.nothing.plugin.file.ResFileHandler
import com.nothing.plugin.file.XmlConstants
import com.nothing.plugin.folder.ResFolderHandler
import com.nothing.plugin.folder.ValuesFolderConstants
import java.io.File

class TranslateResHandler private constructor() {
    fun traverseFolderAndAppendBase(projectResPath: String?, translateResPath: String): String {
        val projectResFolderHandler = ResFolderHandler(projectResPath!!)
        val translateResFolderHandler = ResFolderHandler(translateResPath)
        val projectDefaultStringFiles = getDefaultValuesXmlFiles(projectResFolderHandler)
        val translateDefaultStringFiles = getDefaultValuesXmlFiles(translateResFolderHandler)
        val stringsFileNeedToRename: MutableList<String> = ArrayList()
        val emptyValueMap: MutableMap<String, String> = HashMap()
        val withoutResMap: MutableMap<String, String> = HashMap()
        // 循环project默认字符串资源文件
        for (defaultFile in projectDefaultStringFiles) {
            // 循环project默认字符串资源文件中的字符串内容<string></string>
            for (defaultStringElement in defaultFile.stringContents!!) {
                // 循环翻译字符串资源文件夹
                for (translateValuesFolder in translateResFolderHandler.valuesFolders) {
                    if (ValuesFolderConstants.DEFAULT_STRING_FOLDER == translateValuesFolder) {
                        //默认值的values文件夹不需要检索，以project中的默认资源为准
                        continue
                    }
                    var matchedTranslateStrRes: StringRes? = null
                    var matchedTranslateFile: ResFile? = null
                    val translateValuesHandler =
                        ResFileHandler(translateResFolderHandler.getAbsoluteValuesFolderPath(translateValuesFolder))
                    // 查找每一个翻译字符串资源文件夹下的文件，找到有匹配的 name 就跳出
                    for (translateFile in translateValuesHandler.resFiles) {
                        matchedTranslateStrRes = translateFile.getContainedStringRes(defaultStringElement.key!!)
                        if (null != matchedTranslateStrRes) {
                            if (XmlConstants.EMPTY_VALUE == matchedTranslateStrRes.value) {
                                val emptyStringName =
                                    if (!emptyValueMap.containsKey(translateValuesFolder)) defaultStringElement.toString() else """
     ${emptyValueMap[translateValuesFolder]}
     $defaultStringElement
     """.trimIndent()
                                emptyValueMap[translateValuesFolder] = emptyStringName
                            }
                            matchedTranslateFile = translateFile
                            break
                        } else {
                            if (existInTranslateDefaultValues(translateDefaultStringFiles, defaultStringElement.key)) {
                                val withoutName =
                                    if (!withoutResMap.containsKey(translateValuesFolder)) defaultStringElement.toString() else """
     ${withoutResMap[translateValuesFolder]}
     $defaultStringElement
     """.trimIndent()
                                withoutResMap[translateValuesFolder] = withoutName
                            }
                        }
                    }

                    // 判断查找到的资源文件夹和文件，在 project 中是否已经存在了
                    // 如 project/res 中是否已经有 values-da/strings.xml
                    var existInProjectValues = false
                    var projectExistValueFile: ResFile? = null
                    if (null != matchedTranslateStrRes && projectResFolderHandler.isTargetFolderExist(
                            translateValuesFolder
                        )
                    ) {
                        val projectExistFileHandler =
                            ResFileHandler(projectResFolderHandler.getAbsoluteValuesFolderPath(translateValuesFolder))
                        for (existValuesFile in projectExistFileHandler.resFiles) {
                            val isMatchedExist = isMatchedTranslateFileExist(
                                matchedTranslateFile!!.resFileName,
                                existValuesFile.resFileName
                            )
                            if (isMatchedExist) {
                                projectExistValueFile = existValuesFile
                            }
                            if (existValuesFile.isStringResContained(defaultStringElement.key!!)) {
                                existInProjectValues = true
                                break
                            }
                        }
                    }

                    // 在翻译资源中找到了，并且在 project 中对应的语言下还没有这个字符串
                    val matchedButNotExistInProject = null != matchedTranslateStrRes && !existInProjectValues
                    if (matchedButNotExistInProject) {
                        //System.out.println(defaultStringElement.key + " translate missed, try to append!");
                        if (null != projectExistValueFile) {
                            // 该语言翻译文件已存在，把找到的字符串资源追加进去
                            projectExistValueFile.addAStringRes(matchedTranslateStrRes!!)
                            projectExistValueFile.writeToFile(projectExistValueFile.resFilePath)
                        } else {
                            // 该语言翻译文件还未存在，将 matchedTranslateFile 拷贝过去，但是仅保留当前查找的字符串
                            val projectNewTranslateFile = createNewFileForMatchedTranslateRes(
                                projectResFolderHandler,
                                translateValuesFolder,
                                matchedTranslateStrRes,
                                matchedTranslateFile
                            )
                            if ("nothing_string.xml" == matchedTranslateFile!!.resFileName) {
                                stringsFileNeedToRename.add(projectNewTranslateFile)
                            }
                        }
                    }
                }
            }
        }
        renameNewAddTranslateFileIfNeed(stringsFileNeedToRename)
        val result = generateResultMsg(emptyValueMap, withoutResMap, translateResPath)
        println(result)
        return result
    }

    private fun generateResultMsg(
        emptyMsg: Map<String, String>,
        withoutMsg: Map<String, String>,
        translateResPath: String
    ): String {
        val result = StringBuilder()
        if (!emptyMsg.isEmpty()) {
            result.append("糟糕，这些语言下的字符串是空的 =_= \n")
                .append("-----------------Empty value start------------------\n")
            for ((key, value) in emptyMsg) {
                result.append(key).append(" 语言如下字符串的翻译是空的：\n").append(value).append("\n")
            }
            result.append("-----------------Empty value end------------------\n\n")
        }
        if (!withoutMsg.isEmpty()) {
            result.append("芭比Q了，这些语言都没有翻译 =_= \n")
                .append("-----------------Without translate start------------------\n")
            for ((key, value) in withoutMsg) {
                result.append(key).append(" 语言缺少如下字符串的翻译：\n").append(value).append("\n")
            }
            result.append("-----------------Without translate end------------------\n")
        }
        return if (result.isEmpty()) {
            "完美，所有翻译资源都处理完了 ^_^ !"
        } else {
            val errorMsgFile = "$translateResPath/error_msg.txt"
            writeContentToFile(errorMsgFile, result.toString())
            "糟糕，翻译资源有误 =_=\n 请查看错误信息： $errorMsgFile"
        }
    }

    private fun renameNewAddTranslateFileIfNeed(stringsFileNeedToRename: List<String>) {
        for (renameFile in stringsFileNeedToRename) {
            println("Need to rename file: $renameFile")
            val oldNameFile = File(renameFile)
            if (oldNameFile.exists()) {
                val success = oldNameFile.renameTo(File(oldNameFile.parent + "/" + "strings.xml"))
            }
        }
    }

    private fun createNewFileForMatchedTranslateRes(
        projectResFolderHandler: ResFolderHandler,
        translateValuesFolder: String,
        matchedTranslateStrRes: StringRes?,
        matchedTranslateFile: ResFile?
    ): String {
        //该语言翻译文件还未存在，将 matchedTranslateFile 拷贝过去，但是仅保留当前查找的字符串
        val projectNewTranslateFile = (projectResFolderHandler.getAbsoluteValuesFolderPath(translateValuesFolder) + "/"
                + matchedTranslateFile!!.resFileName)
        println("Add new translate strings file for project: $projectNewTranslateFile")
        val copyContents = ArrayList<StringRes?>()
        copyContents.add(matchedTranslateStrRes)
        matchedTranslateFile.stringContents = ArrayList(copyContents.filterNotNull())
        matchedTranslateFile.writeToFile(projectNewTranslateFile)
        // 在 project 中新创建的文件夹，添加到 projectResFolderHandler 中，以免下一个字符串查找时重复创建
        projectResFolderHandler.addValuesFolder(translateValuesFolder)
        return projectNewTranslateFile
    }

    private fun getDefaultValuesXmlFiles(projectHandler: ResFolderHandler): List<ResFile> {
        val defaultValuesPath = projectHandler.resFolder + "/" + ValuesFolderConstants.DEFAULT_STRING_FOLDER
        return ResFileHandler(defaultValuesPath).resFiles
    }

    private fun isMatchedTranslateFileExist(matchedName: String?, projectExistName: String?): Boolean {
        return matchedName == projectExistName || isNeedToRegularFile(matchedName) && projectExistName == "strings.xml"
    }

    private fun isNeedToRegularFile(matchedName: String?): Boolean {
        return "nothing_string.xml" == matchedName || "arrays.xml" == matchedName || "plurals.xml" == matchedName
    }

    private fun existInTranslateDefaultValues(translateDefaultStringFiles: List<ResFile>, stringKey: String?): Boolean {
        for (tDefaultFile in translateDefaultStringFiles) {
            if (tDefaultFile.isStringResContained(stringKey!!)) {
                return true
            }
        }
        return false
    }

    companion object {
        var instance: TranslateResHandler? = null
            get() {
                if (field == null) {
                    field = TranslateResHandler()
                }
                return field
            }
            private set
    }
}