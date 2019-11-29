package com.smttcn.safebox.ui.settings

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class SettingsViewModel : ViewModel() {

    private val _text = MutableLiveData<String>().apply {
        value = "This is Settings Fragment"
    }
    private val _textChangePassword = MutableLiveData<String>().apply {
        value = "Change Password"
    }

    val text: LiveData<String> = _text
    val textChangePassword: LiveData<String> = _textChangePassword
}