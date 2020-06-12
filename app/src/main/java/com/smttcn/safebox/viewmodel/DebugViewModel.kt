package com.smttcn.safebox.viewmodel

import android.app.Application
import androidx.lifecycle.*
import com.smttcn.commons.Manager.FileManager

class DebugViewModel(application: Application) : AndroidViewModel(application){

    var app : Application

    private val _text = MutableLiveData<String>().apply {
        value = getDebugText()
    }
    val text: LiveData<String> = _text

    init {
        app = application
        initViewModel()
    }

    private fun initViewModel() {
    }

    private fun getDebugText(): String {

        var result: StringBuilder = java.lang.StringBuilder()

        return result.toString()
    }

    private fun getDebugText5(): String {
        var result: StringBuilder = java.lang.StringBuilder()

        val files = FileManager.getFilesInDocumentRoot()

        files.forEach(){
            if (!FileManager.isEncryptedFile(it)) {
                val encFilename = FileManager.encryptFile(it, "111111".toCharArray())
                if (encFilename.length > 5) result.append(encFilename + "\n")
            }
        }

//        if (FileManager.isEncryptedFile(file)) {
//            result.append("Decrypting : " + file.name)
//            FileManager.decryptFile(file, "password".toCharArray())
//        } else {
//            result.append("Encrypted file not found!")
//        }

        return result.toString()
    }

    private fun getDebugText1(): String {
        val path = FileManager.documentDataRoot
        val items = FileManager.getFileDirItemsInDocumentRoot().joinToString("\n--\n")
        return path + "\n\n" + items + "\n-- Last line --"
    }
}