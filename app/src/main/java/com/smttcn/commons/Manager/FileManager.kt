package com.smttcn.commons.Manager

import com.smttcn.commons.helpers.PATH_DOCUMENT_ROOT
import com.smttcn.commons.models.FileDirItem
import com.smttcn.safebox.MyApplication
import java.io.File

object FileManager {

    private var dataFolder: String

    var documentRoot: String
        get() { return MyApplication.getAppContext().filesDir.toString() + "/" + dataFolder }
        private set(value) { dataFolder = value}

    init {
        dataFolder = PATH_DOCUMENT_ROOT
        // create data folder if necessary
        val f = File(documentRoot)
        if (!f.exists() || !f.isDirectory())
            f.mkdir()
    }

    public fun getFileDirItemsInFolder(folder: String = ""): List<FileDirItem> {
        val files = getFilesInFolder(folder)
        var items: MutableList<FileDirItem> = mutableListOf<FileDirItem>()
        for(file in files) {
            items.add(FileDirItem(file))
        }

        return items
    }

    public fun getFilesInFolder(folder: String = "", includeSubfolder: Boolean = false, countHiddenItems: Boolean = false): List<File> {
        val dir = File(toFullPath(folder))

        if (includeSubfolder) {
            return getFilesInSubfolder(dir, countHiddenItems)
        } else {
            dir.listFiles()?.let {
                return it.toList()
            }
            return emptyList()
        }
    }

    private fun getFilesInSubfolder(dir: File, countHiddenItems: Boolean = false): List<File> {
        var items: ArrayList<File> = ArrayList<File>()
        if (dir.exists()) {
            val files = dir.listFiles()
            if (files != null) {
                for (i in files.indices) {
                    if (!files[i].isHidden && !dir.isHidden || countHiddenItems) {
                        if (files[i].exists()) { // check if the file item really exist
                            items.add(files[i]) // add the item
                            if (files[i].isDirectory) {
                                items.addAll(getFilesInSubfolder(files[i], countHiddenItems))
                            }
                        }
                    }
                }
            }
        }
        return items.toList()
    }

    private fun toFullPath(folder: String) : String = documentRoot + folder

    //--------------------------------------------------------------------
    // Dir operation
    public fun isFolderExist(dir: String, isHidden: Boolean = false): Boolean {
        val f = File(toFullPath(dir))
        return f.exists() && f.isDirectory() && f.isHidden() == isHidden
    }

    public fun createFolder(dir: String) {
        val f = File(toFullPath(dir))
        if (!f.exists() || !f.isDirectory())
            f.mkdir()
    }

    //--------------------------------------------------------------------
    // File operations
    fun isFileExist(file: String, isHidden: Boolean = false) : Boolean {
        val f = File(toFullPath(file))
        return f.exists() && f.isHidden() == isHidden
    }

}