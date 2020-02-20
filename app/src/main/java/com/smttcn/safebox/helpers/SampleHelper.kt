package com.smttcn.safebox.helpers

import com.smttcn.commons.Manager.FileManager
import com.smttcn.commons.crypto.Encryption
import com.smttcn.commons.extensions.addExtension
import com.smttcn.commons.extensions.appendPath
import com.smttcn.safebox.MyApplication
import java.io.File

class SampleHelper {
    val sampleImages = arrayOf(
                                        "sample-01.jpg",
                                        "sample-02.jpg",
                                        "sample-03.jpg",
                                        "sample-04.jpg")

    fun Initialze(password: CharArray) {

        if (!IsDocumentRootEmpty()) return

        for (filename in sampleImages) {
            val inputStream = MyApplication.getAppContext().assets.open(filename)
            val encrypt = Encryption()
            val encryptedFilePath = FileManager.documentDataRoot.appendPath(filename.addExtension(FileManager.ENCRYPT_EXT))
            FileManager.EncryptFile(inputStream, filename,encryptedFilePath, password, false)
            inputStream.close()
        }
    }

    fun IsDocumentRootEmpty() : Boolean {
       if (FileManager.getFilesInDocumentRoot().count() > 0)
           return false
        else
           return true
    }
}