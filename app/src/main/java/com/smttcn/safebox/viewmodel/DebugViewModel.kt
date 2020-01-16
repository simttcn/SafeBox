package com.smttcn.safebox.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.smttcn.commons.Manager.FileManager

class DebugViewModel : ViewModel(){
    private val _text = MutableLiveData<String>().apply {
        value = getDebugText()
    }
    val text: LiveData<String> = _text

    private fun getDebugText(): String {
        val path = FileManager.documentRoot
        val items = FileManager.getFileDirItemsInFolder().joinToString("\n--\n")
        return path + "\n\n" + items + "\n-- Last line --"
    }
}