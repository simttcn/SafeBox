package com.smttcn.safebox.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.smttcn.commons.crypto.KeyStoreUtil
import com.smttcn.commons.Manager.FileManager
import com.smttcn.commons.helpers.KEY_STORE_ALIAS

class DebugViewModel : ViewModel(){
    private val _text = MutableLiveData<String>().apply {
        value = getDebugText()
    }
    val text: LiveData<String> = _text

    private fun getDebugText(): String {
        val ks = KeyStoreUtil().getKey(KEY_STORE_ALIAS).toString()
        val path = FileManager.documentRoot
        val items = FileManager.GetFileDirItemsInFolder().joinToString("\n--\n")
        return ks + "\n" + path + "\n\n" + items + "\n-- Last line --"
    }
}