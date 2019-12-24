package com.smttcn.commons.helpers

import com.smttcn.commons.models.FileDirItem
import com.smttcn.safebox.MyApplication
import java.io.File
import java.nio.file.LinkOption

class FileManager {

    lateinit private var _documentRoot: String

    var documentRoot: String
        get() { return MyApplication.getContext().filesDir.toString() + "/" + _documentRoot }
        set(value) {_documentRoot = value}

    init {
        _documentRoot = PATH_DOCUMENT_ROOT
        CheckDirectory(documentRoot, true)
    }

    fun CheckDirectory(dir: String, toCreate: Boolean = false) : Boolean {
        var result: Boolean = false
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

    public fun GetItemsInRootDirectory(): List<File> {
        return GetItemsInDirectory(documentRoot)
    }

    public fun GetItemsInDirectory(dir: String): List<File> {
        val file = File(dir)
        return file.listFiles().toList()
    }

    public fun IsDirectoryExist(dir: String): Boolean {
        return CheckDirectory(dir, false)
    }

    public fun CreateDirectory(dir: String): Boolean {
        return CheckDirectory(dir, true)
    }
}