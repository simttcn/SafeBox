package com.smttcn.commons.Manager

import com.smttcn.commons.crypto.Encryption
import com.smttcn.commons.extensions.addExtension
import com.smttcn.commons.extensions.getParentPath
import com.smttcn.commons.extensions.withTrailingCharacter
import com.smttcn.commons.models.FileDirItem
import com.smttcn.safebox.MyApplication
import java.io.*

object FileManager {

    const val ENCRYPT_EXT = "enc"
    const val DATA_FOLDER = "data"

    var documentRoot: String
        get() { return MyApplication.getAppContext().filesDir.toString().removeSuffix("/") }
        private set(value) {}

    var documentDataRoot: String
        get() { return MyApplication.getAppContext().filesDir.toString().withTrailingCharacter('/') + DATA_FOLDER }
        private set(value) {}

    init {
        // create data folder if necessary
        val f = File(documentDataRoot)
        if (!f.exists() || !f.isDirectory())
            f.mkdir()
    }

    public fun getFileDirItemsInDocumentRoot(folder: String = ""): List<FileDirItem> {
        val files = getFilesInDocumentRoot(folder)
        var items: MutableList<FileDirItem> = mutableListOf<FileDirItem>()
        for(file in files) {
            items.add(FileDirItem(file))
        }

        return items
    }

    fun getFilesInDocumentRoot(folder: String = "", includeSubfolder: Boolean = false, countHiddenItems: Boolean = false): List<File> {
        val dir = File(toFullPathInDocumentRoot(folder))

        if (includeSubfolder) {
            return getFilesInSubfolder(dir, countHiddenItems)
        } else {
            dir.listFiles()?.let {
                return it.sorted().toList()
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
        return items.sorted().toList()
    }

    private fun toFullPathInDocumentRoot(item: String) : String = documentDataRoot.withTrailingCharacter('/') + item

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

    fun isFileExist(file: String, isHidden: Boolean = false) : Boolean {
        val f = File(file)
        return f.exists() && f.isHidden() == isHidden
    }

    fun isEncryptedFile(file: File) : Boolean {
        if (file.isDirectory()) return false

        try {
            ObjectInputStream(FileInputStream(file)).use { it ->
                val data = it.readObject()
                when(data) {
                    is Map<*, *> -> {
                        if (data.containsKey("filename") && data.containsKey("iv") && data.containsKey("salt") && data.containsKey("encrypted")) {
                            return true
                        }
                    }
                }
            }
        } catch (ex: Exception) {
            return false
        }

        return false
    }

    fun EncryptFile(inputStream: InputStream, filename: String, encryptedFilePath: String, password: CharArray, deleteOriginal: Boolean = true) : Boolean {

        try {
            val bytes = inputStream.readBytes()
            val map = Encryption().encryptWithFilename(filename, bytes, password)

            if (map.containsKey("filename") && map.containsKey("iv") && map.containsKey("salt") && map.containsKey("encrypted")) {
                ObjectOutputStream(FileOutputStream(encryptedFilePath)).use {
                        it -> it.writeObject(map)
                }
            }
            return true
        } catch (ex: Exception) {
            return false
        }
    }

    // Todo: to have more fail safe check
    fun EncryptFile(file: File, password: CharArray, deleteOriginal: Boolean = true) : String {
        if (file.isDirectory()) return ""

        val originalFilename = file.name
        val encryptedFilePath = file.path.addExtension(ENCRYPT_EXT)

        val inputStream = file.inputStream()
        val result = EncryptFile(inputStream, originalFilename, encryptedFilePath, password, deleteOriginal)
        inputStream.close()

        if (deleteOriginal && result && isFileExist(encryptedFilePath))
            file.delete()

        return if (result) encryptedFilePath else ""
    }

    // Todo: to have more fail safe check
    fun DecryptFile(file: File, password: CharArray, deleteOriginal: Boolean = true) : String {
        if (file.isDirectory()) return ""

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

            if (isFileExist(decryptedFilePath) && deleteOriginal)
                file.delete()

            return decryptedFilePath

        }

        return ""
    }



}