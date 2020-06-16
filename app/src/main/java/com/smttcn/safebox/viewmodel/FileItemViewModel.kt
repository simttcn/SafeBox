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
import java.io.File
import java.util.*

// Class extends AndroidViewModel and requires application as a parameter.
class FileItemViewModel(application: Application) : AndroidViewModel(application) {

    private var app : Application
    lateinit var allFileItems: MutableLiveData<List<FileDirItem>>

    init {
        app = application
        initViewModel()
    }

    private fun initViewModel() {
        allFileItems = MutableLiveData(refreshDocFolder())
    }

    private fun refreshDocFolder() : List<FileDirItem> {
        return FileManager.getFileDirItemsInDocumentRoot()
    }

    fun refresh() = viewModelScope.launch {
        // note: Important!!! Use setValue() to update value and dispatch change notification to the observer
        allFileItems.setValue(refreshDocFolder())
    }

}