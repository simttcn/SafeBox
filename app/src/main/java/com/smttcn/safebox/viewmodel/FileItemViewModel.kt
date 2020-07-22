package com.smttcn.safebox.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.smttcn.commons.manager.FileManager
import com.smttcn.commons.models.FileDirItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

// Class extends AndroidViewModel and requires application as a parameter.
class FileItemViewModel(application: Application) : AndroidViewModel(application) {

    private var app : Application
    var allFileItems: MutableLiveData<List<FileDirItem>>

    init {
        app = application
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
