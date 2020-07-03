package com.smttcn.safebox.viewmodel

import android.app.Application
import androidx.lifecycle.*
import com.smttcn.commons.manager.FileManager

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


    private fun getDebugText1(): String {
        val path = FileManager.documentDataRoot
        val items = FileManager.getFileDirItemsInDocumentRoot().joinToString("\n--\n")
        return path + "\n\n" + items + "\n-- Last line --"
    }
}