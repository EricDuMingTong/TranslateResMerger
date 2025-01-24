package com.nothing.plugin.folder

import com.nothing.plugin.file.ResFileHandler
import java.io.File

/**
 * 用于翻译文件夹与已存在资源文件夹之间的比对，遍历翻译回来的文件夹：
 * 1. 若工程文件夹已存在同名资源文件夹，此处不做处理，交给 [ResFileHandler] 比对资源文件内容；
 * 2. 若不存在，则创建该资源文件夹
 */
class ResFolderHandler(val resFolder: String) {
    private val mValuesFolders: MutableList<String>

    init {
        mValuesFolders = findAllValuesFolders(resFolder)
    }

    val valuesFolders: List<String>
        get() = mValuesFolders

    fun addValuesFolder(valuesFolderName: String) {
        mValuesFolders.add(valuesFolderName)
    }

    fun isTargetFolderExist(folderName: String): Boolean {
        return mValuesFolders.contains(folderName)
    }

    private fun findAllValuesFolders(resFolderPath: String): MutableList<String> {
        val folder = File(resFolderPath)
        val valueFolders = ArrayList<String>()
        val valuesFolderList = folder.listFiles()
        if (valuesFolderList != null) {
            for (vFolder in valuesFolderList) {
                if (vFolder.isDirectory && isValidValuesFolder(vFolder.name)) {
                    //System.out.println(vFolder.getName());
                    valueFolders.add(vFolder.name)
                }
            }
        }
        return valueFolders
    }

    private fun isValidValuesFolder(folderName: String): Boolean {
        return folderName.startsWith(ValuesFolderConstants.FOLDER_NAME_PREFIX)
    }

    fun getAbsoluteValuesFolderPath(valuesFolderName: String): String {
        return resFolder + "/" + valuesFolderName
    }

    fun createValuesFolder(valuesFolderName: String): String {
        val folderPath = resFolder + "/" + valuesFolderName
        val valuesFolder = File(folderPath)
        if (!valuesFolder.exists()) {
            valuesFolder.mkdirs()
        }
        return folderPath
    }
}