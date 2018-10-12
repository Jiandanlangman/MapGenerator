package com.jiandanlangman.mapgenerator.gui

import java.io.File
import javax.swing.filechooser.FileFilter

class SourceFilesFilter : FileFilter() {

    private fun isImageFile(file: File): Boolean {
        val fileName = file.name
        val index = fileName.lastIndexOf(".")
        if (index != -1) {
            val str = fileName.substring(index)
            return str.equals(".jpg", ignoreCase = true) || str.equals(".png", ignoreCase = true)
        }
        return false
    }


    override fun accept(file: File) = file.isDirectory || file.name == "source.files" || isImageFile(file)

    override fun getDescription() = "*.jpg, *.png, source.files"
}