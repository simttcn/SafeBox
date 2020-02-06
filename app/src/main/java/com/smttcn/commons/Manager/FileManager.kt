package com.smttcn.commons.Manager

import com.smttcn.commons.crypto.Encryption
import com.smttcn.commons.extensions.getParentPath
import com.smttcn.commons.helpers.PATH_DOCUMENT_ROOT
import com.smttcn.commons.models.FileDirItem
import com.smttcn.safebox.MyApplication
import java.io.*

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

    public fun getFileDirItemsInFolderInDocumentRoot(folder: String = ""): List<FileDirItem> {
        val files = getFilesInFolderInDocumentRoot(folder)
        var items: MutableList<FileDirItem> = mutableListOf<FileDirItem>()
        for(file in files) {
            items.add(FileDirItem(file))
        }

        return items
    }

    fun getFilesInFolderInDocumentRoot(folder: String = "", includeSubfolder: Boolean = false, countHiddenItems: Boolean = false): List<File> {
        val dir = File(toFullPathInDocumentRoot(folder))

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

    private fun toFullPathInDocumentRoot(folder: String) : String = documentRoot + folder

    //--------------------------------------------------------------------
    // Dir operation
    fun isFolderExistInDocumentRoot(dir: String, isHidden: Boolean = false): Boolean {
        val f = File(toFullPathInDocumentRoot(dir))
        return f.exists() && f.isDirectory() && f.isHidden() == isHidden
    }

    fun createFolderInDocumentRoot(dir: String) {
        val f = File(toFullPathInDocumentRoot(dir))
        if (!f.exists() || !f.isDirectory())
            f.mkdir()
    }

    //--------------------------------------------------------------------
    // File operations
    fun isFileExistInDocumentRoot(file: String, isHidden: Boolean = false) : Boolean {
        val f = File(toFullPathInDocumentRoot(file))
        return f.exists() && f.isHidden() == isHidden
    }

    fun EncryptFile(file: File, password: CharArray, deleteOriginal: Boolean = true) {
        val originalFilename = file.name
        val encryptedFilePath = file.path + ".enc"

        val inputStream = file.inputStream()
        val bytes = inputStream.readBytes()
        inputStream.close()

        val map = Encryption().encryptWithFilename(originalFilename, bytes, password)
        ObjectOutputStream(FileOutputStream(encryptedFilePath)).use {
                it -> it.writeObject(map)
        }

        if (isFileExistInDocumentRoot(encryptedFilePath) && deleteOriginal)
            file.delete()

    }

    fun DecryptFile(file: File, password: CharArray, deleteOriginal: Boolean = true) {

        var filename: String = ""
        var decryptedFilePath = file.path.getParentPath().removeSuffix("/") + "/"

        var decrypted: ByteArray? = null
        ObjectInputStream(FileInputStream(file)).use { it ->
            val data = it.readObject()

            when(data) {
                is Map<*, *> -> {

                    if (data.containsKey("filename") && data.containsKey("iv") && data.containsKey("salt") && data.containsKey("encrypted")) {
                        val fn = data["filename"]
                        val iv = data["iv"]
                        val salt = data["salt"]
                        val encrypted = data["encrypted"]
                        if (fn is ByteArray && iv is ByteArray && salt is ByteArray && encrypted is ByteArray) {
                            decryptedFilePath += String(fn)
                            decrypted = Encryption().decrypt(
                                hashMapOf("iv" to iv, "salt" to salt, "encrypted" to encrypted), password)
                        }
                    }
                }
            }
        }

        if (decrypted != null) {
            ObjectOutputStream(FileOutputStream(decryptedFilePath)).use {
                    it -> it.writeObject(decrypted)
            }

            if (isFileExistInDocumentRoot(decryptedFilePath) && deleteOriginal)
                file.delete()

        }
    }



}