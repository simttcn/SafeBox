package com.smttcn.safebox.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.smttcn.commons.crypto.KeyStoreUtil
import com.smttcn.commons.helpers.FileManager
import com.smttcn.commons.helpers.KEY_STORE_ALIAS

class MainViewModel : ViewModel(){
    private val _text = MutableLiveData<String>().apply {
        value = "Hello World"
    }
    val text: LiveData<String> = _text

}