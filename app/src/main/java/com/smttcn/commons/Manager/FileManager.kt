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

    public fun GetFileDirItemsInFolder(folder: String = ""): List<FileDirItem> {
        val files = GetFilesInFolder(folder)
        var items: MutableList<FileDirItem> = mutableListOf<FileDirItem>()
        for(file in files) {
            items.add(FileDirItem(file))
        }

        return items
    }

    public fun GetFilesInFolder(folder: String = "", includeSubfolder: Boolean = false, countHiddenItems: Boolean = false): List<File> {
        val dir = File(ToFullPath(folder))

        if (includeSubfolder) {
            return GetFilesInSubfolder(dir, countHiddenItems)
        } else {
            dir.listFiles()?.let {
                return it.toList()
            }
            return emptyList()
        }
    }

    private fun GetFilesInSubfolder(dir: File, countHiddenItems: Boolean = false): List<File> {
        var items: ArrayList<File> = ArrayList<File>()
        if (dir.exists()) {
            val files = dir.listFiles()
            if (files != null) {
                for (i in files.indices) {
                    if (!files[i].isHidden && !dir.isHidden || countHiddenItems) {
                        // Todo: debug step thru and check why data item is icluded
                        if (files[i].exists()) { // check if the file item really exist
                            items.add(files[i]) // add the item
                            if (files[i].isDirectory) {
                                items.addAll(GetFilesInSubfolder(files[i], countHiddenItems))
                            }
                        }
                    }
                }
            }
        }
        return items.toList()
    }

    private fun ToFullPath(folder: String) : String = documentRoot + folder

    //--------------------------------------------------------------------
    // Dir operation
    public fun IsFolderExist(dir: String, isHidden: Boolean = false): Boolean {
        val f = File(ToFullPath(dir))
        return f.exists() && f.isDirectory() && f.isHidden() == isHidden
    }

    public fun CreateFolder(dir: String) {
        val f = File(ToFullPath(dir))
        if (!f.exists() || !f.isDirectory())
            f.mkdir()
    }

    //--------------------------------------------------------------------
    // File operations
    fun IsFileExist(file: String, isHidden: Boolean = false) : Boolean {
        val f = File(ToFullPath(file))
        return f.exists() && f.isHidden() == isHidden
    }

}