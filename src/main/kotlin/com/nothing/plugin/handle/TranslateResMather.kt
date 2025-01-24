package com.nothing.plugin.handle

import com.nothing.plugin.file.ResFile
import com.nothing.plugin.file.ResFileHandler
import com.nothing.plugin.folder.ResFolderHandler
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream

class TranslateResMather {
    var ignoreStringArray = arrayOf(
        "name=\"title_show_google_app\"",
        "name=\"msg_minus_one_on_left\"",
        "name=\"msg_minus_one_on_right\"",
        "name=\"storage_permission_usage_on_wallpaper_section\"",
        "name=\"go_to_settings_btn_text\""
    )
    private val ignoreStringNames: ArrayList<String>? = null
    fun match(translateResPath: String, destResPath: String) {
        println("Translate Path: $translateResPath\nDest Path: $destResPath")
        traverseFolderAndUpdateDest(translateResPath, destResPath)
    }

    private fun traverseFolderAndUpdateDest(translateResFolder: String, destResFolder: String) {
        val translateFolder = ResFolderHandler(translateResFolder)
        val destFolder = ResFolderHandler(destResFolder)
        for (translateValues in translateFolder.valuesFolders) {
            if (destFolder.valuesFolders.contains(translateValues)) {
                traverseFileAndUpdateDest(
                    translateFolder.resFolder + "/" + translateValues,
                    destFolder.resFolder + "/" + translateValues
                )
            } else {
                // TODO 可改进，此处将翻译文件夹下的所有文件循环拷贝过去
                val translateFilePath = translateFolder.resFolder + "/" + translateValues + "/strings.xml"
                val destFilePath = destFolder.createValuesFolder(translateValues) + "/" + "strings.xml"
                copyFileToFolder(translateFilePath, destFilePath)

//                String pluralsFilePath = translateFolder.getResFolder() + "/" + translateValues + "/plurals.xml";
//                String pluralsPath = destFolder.createValuesFolder(translateValues) + "/" + "plurals.xml";
//                copyFileToFolder(pluralsFilePath, pluralsPath);
            }
        }
    }

    private fun traverseFileAndUpdateDest(srcFolder: String, destFolder: String) {
        val translateFiles = ResFileHandler(srcFolder)
        val destFiles = ResFileHandler(destFolder)
        for (srcFile in translateFiles.stringsFiles) {
            if (destFiles.stringsFiles.contains(srcFile)) {
                val translateFile = ResFile(translateFiles.valuesFolder + "/" + srcFile, ignoreStringNames)
                val destFile = ResFile(destFiles.valuesFolder + "/" + srcFile)
                val resultFile = translateFile.compareAndUpdateOrAdd(destFile)
                resultFile.writeToFile(destFiles.valuesFolder + "/" + srcFile)
            } else {
                val translateFilePath = translateFiles.valuesFolder + "/" + srcFile
                val destFilePath = destFiles.valuesFolder + "/" + srcFile
                copyFileToFolder(translateFilePath, destFilePath)
            }
        }
    }

    private fun copyFileToFolder(srcFilePath: String, destFilePath: String) {
        var index: Int
        val bytes = ByteArray(1024)
        try {
            val srcFile = File(srcFilePath)
            val input = FileInputStream(srcFile)
            val outputStream = FileOutputStream(destFilePath)
            while (input.read(bytes).also { index = it } != -1) {
                outputStream.write(bytes, 0, index)
                outputStream.flush()
            }
            outputStream.close()
            input.close()
        } catch (e: Exception) {
            System.err.println("copyFileToFolder,exception" + e.message)
        }
    }
}