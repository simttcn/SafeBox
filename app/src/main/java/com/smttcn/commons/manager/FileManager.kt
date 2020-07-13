package com.smttcn.commons.manager

import android.content.ContentResolver
import android.net.Uri
import android.provider.OpenableColumns
import com.smttcn.commons.crypto.Encryption
import com.smttcn.commons.extensions.*
import com.smttcn.commons.helpers.*
import com.smttcn.commons.models.FileDirItem
import com.smttcn.safebox.MyApplication
import java.io.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap


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
        if (!f.exists() || !f.isDirectory())
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


    fun deleteCache() {
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
        return f.exists() && f.isHidden() == isHidden
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
        var targetFilePath = ""

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

        // we need to update the encrypted hash's stored filename as to be consistent with its actual displayed filename
        if (!filename.equals(targetFilePath.getFilenameFromPath(), true))
            updateEncryptedFileName(File(targetFilePath), targetFilePath.getFilenameFromPath().removeEncryptedExtension())


        return targetFilePath
    }


    // File encrypt/decrypt operations
    fun isEncryptedFileExist(filepath: String, originalFilename: String): Boolean {
        val f = File(filepath)
        if (!isEncryptedFile(f)) return false

        val fn = getFilenameFromEncryptedFile(f)

        return originalFilename.equals(fn)
    }


    fun isEncryptedFile(file: File): Boolean {
        if (!file.exists() || file.isDirectory()) return false

        try {
            ObjectInputStream(FileInputStream(file)).use { it ->
                val map = getEncryptedHashMap(it.readObject())
                if (isValidEncryptedHashMap(map)) {
                    return true
                }
            }
        } catch (ex: Exception) {
            return false
        }

        return false
    }


    fun isEncryptedFileUri(contentResolver: ContentResolver, uri: Uri): Boolean {

        try {
            ObjectInputStream(contentResolver.openInputStream(uri)).use { it ->
                val map = getEncryptedHashMap(it.readObject())
                if (isValidEncryptedHashMap(map)) {
                    return true
                }
            }
        } catch (ex: Exception) {
            return false
        }

        return false
    }


    fun getFilenameFromEncryptedFile(file: File): String {
        if (file.isDirectory()) return ""

        try {
            ObjectInputStream(FileInputStream(file)).use { it ->
                val map = getEncryptedHashMap(it.readObject())
                if (map != null) {
                    val fn = map[APP_ENCRYPT_VAR0]
                    if (fn is ByteArray) {
                        return String(fn).getFilenameFromPath()
                    }
                }
            }
        } catch (ex: Exception) {
            return ""
        }

        return ""
    }


    fun getFileExtensionFromEncryptedFile(file: File): String {
        return getFilenameFromEncryptedFile(file).getFileExtension()
    }

    fun writeEncryptedHashMapToFile(map: HashMap<String, ByteArray>, filePath: String): Boolean {

        if (!isValidEncryptedHashMap(map)) return false

        try {

            ObjectOutputStream(FileOutputStream(filePath)).use { it ->
                it.writeObject(map)
            }
            return true
        } catch (ex: Exception) {

            return false
        }

    }


    fun updateEncryptedFileName(file: File, newFilename: String): Boolean {
        if (file.isDirectory()) return false

        try {
            ObjectInputStream(FileInputStream(file)).use { it ->
                val map = getEncryptedHashMap(it.readObject())
                if (map != null) {
                    val fn = map[APP_ENCRYPT_VAR0]
                    if (fn is ByteArray) {
                        // update the hashmap object with the new filename
                        map[APP_ENCRYPT_VAR0] = newFilename.toByteArray()

                        return writeEncryptedHashMapToFile(map, file.canonicalPath)

                    }
                }
            }
        } catch (ex: Exception) {
            return false
        }


        return false
    }


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

        //todo later: got to check inputStream validity
        val bytes = inputStream!!.readBytes()
        inputStream.close()
        // perform the encryption with the filename
        val map = Encryption().encryptWithFilename(encryptedFilePath.getUnencryptedFilenameFromPath(), bytes, password)

        if (writeEncryptedHashMapToFile(map, encryptedFilePath))
            return encryptedFilePath
        else
            return ""

    }


    // re-encrypt files with new password
    fun reencryptFiles(filePath: String, oldPwd: CharArray, newPwd: CharArray): Boolean {

        // Todo future: should decrypt to hashmap in memory and then encrypt it back to file
        // decrypt file with delete original
        val decryptedFilePath = decryptFile(filePath, password = oldPwd, deleteOriginal = true)
        // encrypt file with delete original
        val encryptedFilePath = encryptFile(decryptedFilePath, filePath, newPwd, true)

        if (isEncryptedFile(File(encryptedFilePath))) {
            return true
        } else {
            return false
        }

    }


    fun encryptFile(inputStream: InputStream, filename: String, targetFilePath: String, password: CharArray): Boolean {

        try {
            val bytes = inputStream.readBytes()
            val map = Encryption().encryptWithFilename(filename, bytes, password)

            return writeEncryptedHashMapToFile(map, targetFilePath)

        } catch (ex: Exception) {
            return false
        }
    }


    fun encryptFile(sourceFilePath: String, targetFilePath: String, password: CharArray, deleteOriginal: Boolean = true): String {

        val sourceFile = File(sourceFilePath)

        if (sourceFile.exists() && !sourceFile.isDirectory()) {

            val originalFilename = sourceFile.name
            var encryptedFilePath = targetFilePath.removeEncryptedExtension().addExtension(ENCRYPTED_FILE_EXT)

            encryptedFilePath = renameDuplicatedFilePath(encryptedFilePath)

            try {
                val inputStream = sourceFile.inputStream()
                val result = encryptFile(inputStream, originalFilename, encryptedFilePath, password)
                inputStream.close()

                if (deleteOriginal && result && isFileExist(encryptedFilePath))
                    sourceFile.delete()

                return if (result) encryptedFilePath else ""
            } catch (ex: Exception) {
                return ""
            }

        } else {

            return ""

        }
    }


    fun decryptFile(objectInputStream: ObjectInputStream, targetFolder: String = "", password: CharArray, deleteExisting: Boolean = true): String {

        var decryptedFilePath: String

        if (targetFolder.length > 0)
            decryptedFilePath = targetFolder.withTrailingCharacter('/')
        else
            // default decrypting folder will be in app cache folder
            decryptedFilePath = getFolderInCacheFolder()!!.canonicalPath.withTrailingCharacter('/')

        var decrypted: ByteArray? = null
        val map = getEncryptedHashMap(objectInputStream.readObject())

        if (map != null) {
            val fn = map[APP_ENCRYPT_VAR0]
            val iv = map[APP_ENCRYPT_VAR1]
            val salt = map[APP_ENCRYPT_VAR2]
            val encrypted = map[APP_ENCRYPT_VAR3]
            if (fn is ByteArray && iv is ByteArray && salt is ByteArray && encrypted is ByteArray) {
                // try remove the encrypted extension just in case
                decryptedFilePath += String(fn).removeEncryptedExtension()
                decrypted = Encryption().decrypt(
                    hashMapOf(APP_ENCRYPT_VAR1 to iv, APP_ENCRYPT_VAR2 to salt, APP_ENCRYPT_VAR3 to encrypted), password
                )
            }

            if (decrypted != null) {

                // do we need to delete the existing file?
                if (isFileExist(decryptedFilePath) && deleteExisting)
                    File(decryptedFilePath).delete()

                // write decrypted data out as binary file object
                FileOutputStream(decryptedFilePath).use { it ->
                    it.write(decrypted)
                }

                return decryptedFilePath

            }
        }

        return ""
    }


    fun decryptFile(sourceFilePath: String, targetFolder: String = "", password: CharArray, deleteOriginal: Boolean = true): String {
        val sourceFile = File(sourceFilePath)

        if (sourceFile.exists() && !sourceFile.isDirectory()){

            try {
                var decrypted: ByteArray? = null
                ObjectInputStream(FileInputStream(sourceFile)).use { it ->
                    val decryptedFilePath = decryptFile(it, targetFolder, password, deleteExisting = true)

                    if (decryptedFilePath.length > 0 && deleteOriginal)
                        // only delete the original if the file was decrypted successfully
                        sourceFile.delete()

                    return decryptedFilePath
                }

            } catch (ex: Exception) {
                return ""
            }

        } else {

            return ""

        }

        return ""
    }


    fun decryptFileContentToByteArray(file: File, password: CharArray): ByteArray? {
        if (file.isDirectory()) return null

        try {
            var decrypted: ByteArray? = null
            ObjectInputStream(FileInputStream(file)).use { it ->
                val map = getEncryptedHashMap(it.readObject())
                if (map != null) {
                    val fn = map[APP_ENCRYPT_VAR0]
                    val iv = map[APP_ENCRYPT_VAR1]
                    val salt = map[APP_ENCRYPT_VAR2]
                    val encrypted = map[APP_ENCRYPT_VAR3]
                    if (fn is ByteArray && iv is ByteArray && salt is ByteArray && encrypted is ByteArray) {
                        decrypted = Encryption().decrypt(
                            hashMapOf(APP_ENCRYPT_VAR1 to iv, APP_ENCRYPT_VAR2 to salt, APP_ENCRYPT_VAR3 to encrypted), password
                        )
                    }
                }
            }

            return decrypted

        } catch (ex: Exception) {
            return null
        }
    }


    fun decryptFileForSharing(pwd: CharArray, filePath: String): String {

        var decryptedFilepath: String = ""
        val sourcefile = File(filePath)

        if (!sourcefile.exists())
            return decryptedFilepath

        val targetPath = getFolderInCacheFolder(TEMP_FILE_SHARE_FOLDER_NAME, true)
        if (sourcefile.length() > 0 && targetPath != null) {
            decryptedFilepath =
                decryptFile(filePath, targetPath.canonicalPath, pwd, false)
        }

        return decryptedFilepath

    }


    fun decryptFileForSharing(pwd: CharArray, objectInputStream: ObjectInputStream): String {

        var decryptedFilepath: String = ""

        val targetFolder = getFolderInCacheFolder(TEMP_FILE_SHARE_FOLDER_NAME, true)
        if (targetFolder != null) {
            decryptedFilepath = decryptFile(objectInputStream, targetFolder.canonicalPath, pwd, true)
        }

        return decryptedFilepath

    }


    // helper methods
    private fun getEncryptedHashMap(data: Any): HashMap<String, ByteArray>? {
        try {
            when (data) {
                is HashMap<*, *> -> {
                    @Suppress("UNCHECKED_CAST")
                    return data as? HashMap<String, ByteArray>
                }
            }
        } catch (ex: java.lang.Exception) {
            return null
        }

        return null
    }


    private fun isValidEncryptedHashMap(hashMap: HashMap<String, ByteArray>?): Boolean {

        if (hashMap != null) {
            if (hashMap.containsKey(APP_ENCRYPT_VAR0)
                && hashMap.containsKey(APP_ENCRYPT_VAR1)
                && hashMap.containsKey(APP_ENCRYPT_VAR2)
                && hashMap.containsKey(APP_ENCRYPT_VAR3)
            ) {
                return true
            }
        }

        return false
    }

}