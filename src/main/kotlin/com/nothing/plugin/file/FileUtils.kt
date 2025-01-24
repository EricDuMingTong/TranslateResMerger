package com.nothing.plugin.file

import java.io.BufferedWriter
import java.io.File
import java.io.IOException
import java.io.OutputStreamWriter
import java.nio.charset.StandardCharsets
import java.nio.file.Files

object FileUtils {
    fun createFileIfNotExist(filePath: String?) {
        val file = File(filePath)
        try {
            if (!file.parentFile.exists()) {
                file.parentFile.mkdirs()
            }
            if (!file.exists()) {
                file.createNewFile()
            }
        } catch (e: IOException) {
            throw RuntimeException(e)
        }
    }

    fun writeContentToFile(fileName: String?, content: String?) {
        createFileIfNotExist(fileName)
        try {
            val bw = BufferedWriter(
                OutputStreamWriter(
                    Files.newOutputStream(File(fileName).toPath()),
                    StandardCharsets.UTF_8
                )
            )
            bw.write(content)
            bw.flush()
            bw.close()
        } catch (e: IOException) {
            // TODO Auto-generated catch block
            e.printStackTrace()
        }
    }
}