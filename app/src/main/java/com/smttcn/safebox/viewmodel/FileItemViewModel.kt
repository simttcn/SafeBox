package com.smttcn.safebox.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.smttcn.commons.Manager.FileManager
import com.smttcn.commons.models.FileDirItem
import com.smttcn.safebox.MyApplication
import kotlinx.coroutines.launch

// Class extends AndroidViewModel and requires application as a parameter.
class FileItemViewModel(application: Application) : AndroidViewModel(application) {

    var app : Application
    lateinit var allFileItems: LiveData<List<FileDirItem>>

    init {
        app = application
        initViewModel()
    }

    fun reload() {
        initViewModel()
    }

    private fun initViewModel() {
        var allItems : MutableLiveData<List<FileDirItem>>

        allItems = MutableLiveData<List<FileDirItem>>(FileManager.getFileDirItemsInDocumentRoot())

        allFileItems = allItems
    }

}