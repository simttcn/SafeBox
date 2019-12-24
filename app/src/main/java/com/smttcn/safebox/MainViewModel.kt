package com.smttcn.safebox

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.smttcn.commons.crypto.KeyStoreUtil
import com.smttcn.commons.helpers.FileManager
import com.smttcn.commons.helpers.KEY_STORE_ALIAS

class MainViewModel : ViewModel(){
    private val _text = MutableLiveData<String>().apply {
        value = getSecretKey()
    }
    val text: LiveData<String> = _text

    private fun getSecretKey(): String {
        val fm = FileManager()
        val ks = KeyStoreUtil().getKey(KEY_STORE_ALIAS).toString()
        val path = fm.documentRoot
        val items = fm.GetItemsInRootDirectory().joinToString("\n--\n")
        return ks + "\n" + path + "\n\n" + items + "\n-- Last line --"
    }
}