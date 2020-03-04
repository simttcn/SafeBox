package com.smttcn.commons.Manager

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import com.smttcn.commons.crypto.Encryption
import com.smttcn.commons.extensions.addExtension
import com.smttcn.commons.extensions.getFilenameFromPath
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

    @Suppress("UNUSED_PARAMETER")
    var documentDataRoot: String
        get() { return MyApplication.getAppContext().filesDir.canonicalPath.withTrailingCharacter('/') + DATA_FOLDER }
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

    fun toFullPathInDocumentRoot(itemName: String) : String = documentDataRoot.withTrailingCharacter('/') + itemName

    //--------------------------------------------------------------------
    // Dir operation
    fun isFolderExist(file: String, isHidden: Boolean = false) : Boolean {
        val f = File(file)
        return f.isDirectory() && f.exists() && f.isHidden() == isHidden
    }

    fun isFolderExistInDocumentRoot(dir: String, isHidden: Boolean = false): Boolean {
        val f = File(toFullPathInDocumentRoot(dir))
        return f.exists() && f.isDirectory() && f.isHidden() == isHidden
    }

    fun createFolderInDocumentRoot(dir: String) {
        val f = File(toFullPathInDocumentRoot(dir))
        if (!f.exists() || !f.isDirectory())
            f.mkdir()
    }

    fun getFolderInCacheFolder(dir: String, toCreate: Boolean = false): File? {
        val f = File(MyApplication.getAppContext().cacheDir.canonicalPath.withTrailingCharacter('/') + dir)

        if (f.exists() && f.isDirectory()) {
            return f
        } else {
            if (toCreate) {
                f.mkdir()
                return f
            }
        }

        return null
    }

    fun deleteCache(context: Context) {
        try {
            val cacheDir: File = context.getCacheDir()
            val dir = File(cacheDir.canonicalPath)
            deleteDir(dir, true)
        } catch (e: java.lang.Exception) {
            //e.printStackTrace()
        }
    }

    fun deleteDir(dir: File?, deleteNotEmpty: Boolean = false): Boolean {
        return if (dir != null && dir.isDirectory) {
            val children = dir.list()
            if (children.count() > 0 && deleteNotEmpty) {
                for (i in children.indices) {
                    val success = deleteDir(File(dir, children[i]), deleteNotEmpty)
                    if (!success) {
                        return false
                    }
                }
            }
            dir.delete()
        } else if (dir != null && dir.isFile) {
            dir.delete()
        } else {
            false
        }
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

    fun copyFileFromUriToFolder(contentResolver: ContentResolver, uri: Uri, folder: String = "") {
        val (filename, size) = getFilenameAndSizeFromUri(contentResolver, uri)

        if (size < 1) return

        val inputStream = contentResolver.openInputStream(uri)
        var targetFilePath: String

        if (folder.length > 0)
            targetFilePath =  FileManager.toFullPathInDocumentRoot(folder.withTrailingCharacter('/') + filename)
        else
            targetFilePath =  FileManager.toFullPathInDocumentRoot(filename)

        FileOutputStream(targetFilePath).use { fileOut ->
            inputStream!!.copyTo(fileOut)
            fileOut.close()
        }
        inputStream!!.close()
    }

    fun encryptFileFromUriToFolder(contentResolver: ContentResolver, password: CharArray, uri: Uri, folder: String = "") : String {
        var encryptedFilePath: String
        val (filename, size) = getFilenameAndSizeFromUri(contentResolver, uri)

        if (size < 1) return ""

        val inputStream = contentResolver.openInputStream(uri)

        if (folder.length > 0)
            encryptedFilePath =  FileManager.toFullPathInDocumentRoot(folder.withTrailingCharacter('/') + filename)
        else
            encryptedFilePath =  FileManager.toFullPathInDocumentRoot(filename)

        encryptedFilePath = encryptedFilePath + ".enc"

        //todo: got to check inputStream vaidity
        val bytes = inputStream!!.readBytes()
        inputStream.close()
        val map = Encryption().encryptWithFilename(filename, bytes, password)

        if (map.containsKey("filename") && map.containsKey("iv") && map.containsKey("salt") && map.containsKey("encrypted")) {
            ObjectOutputStream(FileOutputStream(encryptedFilePath)).use {
                    it -> it.writeObject(map)
            }

            return encryptedFilePath
        }

        return ""

    }

    fun getFilenameAndSizeFromUri(contentResolver: ContentResolver, uri: Uri) : Pair<String, Long> {
        /*
         * Get the file's content URI from the incoming Intent,
         * then query the server app to get the file's display name
         * and size.
         */
        var result = Pair("", 0L)
        contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            /*
             * Get the column indexes of the data in the Cursor,
             * move to the first row in the Cursor, get the data,
             * and display it.
             */
            val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            val sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE)
            cursor.moveToFirst()
            result = Pair(cursor.getString(nameIndex), cursor.getLong(sizeIndex))
        }

        return result
    }

    fun isEncryptedFileExist(filepath: String, originalFilename: String) : Boolean {
        val f = File(filepath)
        if (!isEncryptedFile(f)) return false

        val fn = GetFilenameFromEncryptedFile(f)

        return originalFilename.equals(fn)
    }

    fun isEncryptedFile(file: File) : Boolean {
        if (!file.exists() || file.isDirectory()) return false

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

    fun GetFilenameFromEncryptedFile(file: File) : String {
        if (file.isDirectory()) return ""

        try {
            ObjectInputStream(FileInputStream(file)).use { it ->
                val data = it.readObject()
                when(data) {
                    is Map<*, *> -> {
                        if (data.containsKey("filename") && data.containsKey("iv") && data.containsKey("salt") && data.containsKey("encrypted")) {
                            val fn = data["filename"]
                            if (fn is ByteArray)
                                return String(fn).getFilenameFromPath()
                        }
                    }
                }
            }
        } catch (ex: Exception) {
            return ""
        }

        return ""
    }

    fun EncryptFile(inputStream: InputStream, filename: String, encryptedFilePath: String, password: CharArray) : Boolean {

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

    fun EncryptFile(file: File, password: CharArray, deleteOriginal: Boolean = true) : String {
        if (file.isDirectory()) return ""

        val originalFilename = file.name
        val encryptedFilePath = file.path.addExtension(ENCRYPT_EXT)

        try {
            val inputStream = file.inputStream()
            val result = EncryptFile(inputStream, originalFilename, encryptedFilePath, password)
            inputStream.close()

            if (deleteOriginal && result && isFileExist(encryptedFilePath))
                file.delete()

            return if (result) encryptedFilePath else ""
        } catch (ex: Exception) {
            return ""
        }
    }

    fun DecryptFile(file: File, password: CharArray, targetFolder: String = "", deleteOriginal: Boolean = true) : String {
        if (file.isDirectory()) return ""

        var decryptedFilePath = ""

        if (targetFolder.length > 0)
            decryptedFilePath = targetFolder.withTrailingCharacter('/')
        else
            decryptedFilePath = file.path.getParentPath().withTrailingCharacter('/')


        try {
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
                // write decrypted data out as binary file object
                FileOutputStream(decryptedFilePath).use {
                        it -> it.write(decrypted)
                }

                if (isFileExist(decryptedFilePath) && deleteOriginal)
                    file.delete()

                return decryptedFilePath

            }

            return ""

        } catch (ex: Exception) {
            return ""
        }
    }



}