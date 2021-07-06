package com.smttcn.commons.manager

import android.content.ContentResolver
import android.net.Uri
import android.provider.OpenableColumns
import com.smttcn.crypto.Encryption
import com.smttcn.crypto.APP_ENCRYPT_VAR0
import com.smttcn.crypto.APP_ENCRYPT_VAR1
import com.smttcn.crypto.APP_ENCRYPT_VAR2
import com.smttcn.crypto.APP_ENCRYPT_VAR3
import com.smttcn.commons.extensions.*
import com.smttcn.commons.helpers.*
import com.smttcn.commons.models.FileDirItem
import com.smttcn.safebox.MyApplication
import java.io.*
import java.util.*
import kotlin.collections.ArrayList


object FileManager {

    const val DATA_FOLDER = "data"

    @Suppress("UNUSED_PARAMETER")
    var documentRoot: String
        get() {
            return MyApplication.applicationContext.filesDir.toString().removeSuffix("/")
        }
        private set(value) {}

    @Suppress("UNUSED_PARAMETER")
    var documentDataRoot: String
        get() {
            return MyApplication.applicationContext.filesDir.canonicalPath.withTrailingCharacter('/') + DATA_FOLDER
        }
        private set(value) {}


    init {
        // create data folder if necessary
        val f = File(documentDataRoot)
        if (!f.exists() || !f.isDirectory)
            f.mkdir()
    }


    fun getFileDirItemsInDocumentRoot(folder: String = ""): List<FileDirItem> {
        val files = getFilesInDocumentRoot(folder)
        var items: MutableList<FileDirItem> = mutableListOf<FileDirItem>()
        for (file in files) {
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


    private fun toFullPathInDocumentRoot(itemName: String): String = documentDataRoot.withTrailingCharacter('/') + itemName


    // Dir operation
    fun isFolderExist(file: String, isHidden: Boolean = false): Boolean {
        val f = File(file)
        return f.isDirectory && f.exists() && f.isHidden == isHidden
    }


    fun isFolderExistInDocumentRoot(dir: String, isHidden: Boolean = false): Boolean {
        val f = File(toFullPathInDocumentRoot(dir))
        return f.exists() && f.isDirectory && f.isHidden == isHidden
    }


    fun createFolderInDocumentRoot(dir: String) {
        val f = File(toFullPathInDocumentRoot(dir))
        if (!f.exists() || !f.isDirectory)
            f.mkdir()
    }


    fun getFolderInCacheFolder(dir: String = "", toCreate: Boolean = false): File? {
        val f = File(MyApplication.applicationContext.cacheDir.canonicalPath.withTrailingCharacter('/') + dir)

        if (f.exists() && f.isDirectory) {
            return f
        } else {
            if (toCreate) {
                f.mkdir()
                return f
            }
        }

        return null
    }


    fun emptyCacheFolder() {
        try {
            val cacheFolder = MyApplication.applicationContext.cacheDir
            val children: Array<String>? = cacheFolder.list()
            if (children != null) {
                for (i in children.indices) {
                    File(cacheFolder, children[i]).delete()
                }
            }
        } catch (e: java.lang.Exception) {
            //e.printStackTrace()
        }
    }


    fun deleteCacheShareFolder() {
        try {
            val cacheShareDir = getFolderInCacheFolder(TEMP_FILE_SHARE_FOLDER_NAME)
            deleteDir(cacheShareDir, true)
        } catch (e: java.lang.Exception) {
            //e.printStackTrace()
        }
    }


    fun deleteDir(dir: File?, deleteRecursively: Boolean = false): Boolean {
        return if (dir != null && dir.isDirectory) {
            if (deleteRecursively)
                dir.deleteRecursively()
            else
                dir.delete()
        } else {
            false
        }
    }


    // File operations
    fun isFileExistInDocumentRoot(file: String, isHidden: Boolean = false): Boolean {
        val f = File(toFullPathInDocumentRoot(file))
        return f.exists() && f.isHidden == isHidden
    }


    fun isFileExist(file: String, isHidden: Boolean = false): Boolean {
        val f = File(file)
        return f.exists() && f.isHidden == isHidden
    }


    private fun renameDuplicatedFilePath(filePath: String): String {

        if (isFileExist(filePath)) {

            var filePathWithoutEncExt = filePath.removeSuffix("." + ENCRYPTED_FILE_EXT)
            var counter = 1
            var newFilename = filePathWithoutEncExt
                .insertBeforeFileExtension(DUPLICATED_FILE_SUFFIX + counter.toString()) + "." + ENCRYPTED_FILE_EXT

            while (isFileExist(newFilename)) {
                counter++
                newFilename = filePathWithoutEncExt
                    .insertBeforeFileExtension(DUPLICATED_FILE_SUFFIX + counter.toString()) + "." + ENCRYPTED_FILE_EXT
            }
            return newFilename

        } else {
            return filePath
        }


    }


    fun deleteFile(file: File?): Boolean {
        if (file != null && file.isFile) {
            return file.delete()
        } else {
            return false
        }
    }


    fun copyFileToTempShareFolder(sourceFilePath: String): String {
        var sourceFile = File(sourceFilePath)

        if (sourceFile.exists()) {
            val targetFolder = getFolderInCacheFolder(TEMP_FILE_SHARE_FOLDER_NAME, true)
            if (targetFolder != null) {
                var copiedFile = sourceFile.copyToFolder(targetFolder)

                if (copiedFile.exists())
                    return copiedFile.canonicalPath
            }
        }
        return ""
    }


    fun getFilenameAndSizeFromUri(contentResolver: ContentResolver, uri: Uri): Pair<String, Long> {
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


    fun copyFileFromUriToFolder(contentResolver: ContentResolver, uri: Uri, folder: String = ""): String {
        val (filename, size) = getFilenameAndSizeFromUri(contentResolver, uri)

        if (size < 1) return ""

        val inputStream = contentResolver.openInputStream(uri)
        var targetFilePath: String

        if (folder.length > 0)
            targetFilePath = FileManager.toFullPathInDocumentRoot(folder.withTrailingCharacter('/') + filename)
        else
            targetFilePath = FileManager.toFullPathInDocumentRoot(filename)

        targetFilePath = renameDuplicatedFilePath(targetFilePath)

        FileOutputStream(targetFilePath).use { fileOut ->
            inputStream!!.copyTo(fileOut)
            fileOut.close()
        }
        inputStream!!.close()

        return targetFilePath
    }


    // File encrypt/decrypt operations
    fun encryptFileFromUriToFolder(contentResolver: ContentResolver, password: CharArray, uri: Uri, folder: String = ""): String {
        var encryptedFilePath: String
        val (filename, size) = getFilenameAndSizeFromUri(contentResolver, uri)

        if (size < 1) return ""

        val inputStream = contentResolver.openInputStream(uri)

        if (folder.length > 0)
            encryptedFilePath = FileManager.toFullPathInDocumentRoot(folder.withTrailingCharacter('/') + filename)
        else
            encryptedFilePath = FileManager.toFullPathInDocumentRoot(filename)

        encryptedFilePath = encryptedFilePath.addExtension(ENCRYPTED_FILE_EXT)

        encryptedFilePath = renameDuplicatedFilePath(encryptedFilePath)

        if (inputStream != null) {

            if (Encryption(ENCRYPTION_VER).encryptInputStreamToFile(inputStream, encryptedFilePath, password))
                return encryptedFilePath
        }

        return ""

    }


    // re-encrypt files with new password
    fun reencryptFiles(filePath: String, oldPwd: CharArray, newPwd: CharArray): Boolean {

        val sourceFile = File(filePath)

        if (sourceFile.exists() && !sourceFile.isDirectory) {

            try {
                val tempFilepath = MyApplication.applicationContext.cacheDir.canonicalPath.withTrailingCharacter('/') + "tmp_" + sourceFile.name.removeEncryptedExtension()

                val enc = Encryption(ENCRYPTION_VER)
                if (enc.decryptFileToFile(filePath, tempFilepath, oldPwd)) {
                    // decrypted, so encrypt with new password
                    val result = enc.encryptFileToFile(tempFilepath, filePath, newPwd, true)

                    if (result) {

                        File(tempFilepath).delete()

                        return true
                    }
                }

            } catch (ex: Exception) {

                return false

            }
        }

        return false

    }



    fun decryptFileToByteArray(file: File, password: CharArray): ByteArray? {
        if (file.isDirectory) return null

        return Encryption(ENCRYPTION_VER).decryptFileToByteArray(file.canonicalPath, password)
    }


    fun decryptFileForSharing(sourceFilePath: String, pwd: CharArray): String {

        var targetFilepath: String = ""
        val sourcefile = File(sourceFilePath)

        if (!sourcefile.exists())
            return targetFilepath

        val targetFolder = getFolderInCacheFolder(TEMP_FILE_SHARE_FOLDER_NAME, true)!!.canonicalPath.withTrailingCharacter('/')
        targetFilepath = targetFolder + sourceFilePath.getFilenameFromPath().removeEncryptedExtension()

        if (targetFilepath.length > 0) {
            val result = Encryption(ENCRYPTION_VER).decryptInputStreamToFile(FileInputStream(sourceFilePath), targetFilepath.removeEncryptedExtension(), pwd, true)

            if (result == true) {

                return targetFilepath

            }
        }

        return ""

    }


    fun decryptInputStreamForSharing(inputStream: InputStream, filename : String, pwd: CharArray): String {

        val tempShareFolder: File? = getFolderInCacheFolder(TEMP_FILE_SHARE_FOLDER_NAME, true)

        if (tempShareFolder != null) {

            val targetFilepath = tempShareFolder.canonicalPath.withTrailingCharacter('/') + filename.removeEncryptedExtension()

            val result = Encryption(ENCRYPTION_VER).decryptInputStreamToFile(inputStream, targetFilepath.removeEncryptedExtension(), pwd, true)

            if (result == true) {

                return targetFilepath

            }
        }

        return ""

    }


}