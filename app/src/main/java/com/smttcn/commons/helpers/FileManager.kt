package com.smttcn.commons.helpers

import com.smttcn.commons.models.FileDirItem
import com.smttcn.safebox.MyApplication
import java.io.File

class FileManager {

    var mDocumentRoot: String

    var documentRoot: String
        get() { return MyApplication.getContext().filesDir.toString() + "/" + mDocumentRoot }
        set(value) {mDocumentRoot = value}

    init {
        mDocumentRoot = PATH_DOCUMENT_ROOT
        CheckDirectory(documentRoot, true)
    }

    fun CheckDirectory(dir: String, toCreate: Boolean = false) : Boolean {
        val f = File(dir)
        if (f.exists() && f.isDirectory()) {
            return true
        } else {
            if (toCreate) {
                return f.mkdir()
            } else {
                return false
            }
        }
    }

    public fun GetFileDirItemsInRootDirectory(): List<FileDirItem> {
        val files = GetFilesInDirectory(documentRoot)
        var items: MutableList<FileDirItem> = mutableListOf<FileDirItem>()
        for(file in files) {
            items.add(FileDirItem(file))
        }

        return items
    }

    public fun GetFilesInRootDirectory(): List<File> {
        return GetFilesInDirectory(documentRoot)
    }

    public fun GetFilesInDirectory(dir: String): List<File> {
        val file = File(dir)
        file.listFiles()?.let {
            return it.toList()
        }
        return emptyList()
    }

    public fun IsDirectoryExist(dir: String): Boolean {
        return CheckDirectory(dir, false)
    }

    public fun CreateDirectory(dir: String): Boolean {
        return CheckDirectory(dir, true)
    }
}