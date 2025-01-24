package com.nothing.plugin.file

import java.io.File

/**
 * 用于比对翻译回来的资源文件及原来工程资源文件内容, 需要同时对 ID 及内容进行比对：
 * 1. 若原有文件不存在翻译回来的id，则直接追加在原有文件中；
 * 2. 若原有文件存在该id，则对id对应的内容进行比较，内容一致则无需处理；内容不一致，则对原有文件进行更新；
 */
class ResFileHandler(val valuesFolder: String) {
    val stringsFiles: List<String>

    init {
        stringsFiles = findAllStringsFiles(valuesFolder)
    }

    val resFiles: List<ResFile>
        get() {
            val valuesFiles = ArrayList<ResFile>()
            for (srcFile in stringsFiles) {
                val file = ResFile(valuesFolder + "/" + srcFile)
                valuesFiles.add(file)
            }
            return valuesFiles
        }

    private fun findAllStringsFiles(valuesFolder: String): List<String> {
        val folder = File(valuesFolder)
        val stringsFiles = ArrayList<String>()
        val files = folder.listFiles()
        if (files != null) {
            for (stringsFile in files) {
                if (stringsFile.isFile && XmlConstants.isValidStringsFileName(stringsFile.name)) {
                    //System.out.println(stringsFile.getName());
                    stringsFiles.add(stringsFile.name)
                }
            }
        }
        return stringsFiles
    }
}