package com.smttcn.safebox.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.smttcn.safebox.database.AppDatabase
import com.smttcn.safebox.database.DbItem
import com.smttcn.safebox.database.DbItemRepository
import kotlinx.coroutines.launch

// Class extends AndroidViewModel and requires application as a parameter.
class DbItemViewModel(application: Application) : AndroidViewModel(application) {

    // The ViewModel maintains a reference to the repository to get data.
    lateinit private var repo: DbItemRepository
    // LiveData gives us updated words when they change.
    var allDbItems: LiveData<List<DbItem>>

    init {
        // Gets reference to DbItemDao from AppDatabase to construct
        // the correct DbItemRepository.
        if (AppDatabase.getDb(application, viewModelScope) != null) {
            val dbItemDao = AppDatabase.getDb(application, viewModelScope)!!.dbItemDao()
            repo = DbItemRepository(dbItemDao)
            allDbItems = repo.allDbItems
        } else {
            allDbItems = MutableLiveData()
        }
    }

    /**
     * The implementation of insert() in the database is completely hidden from the UI.
     * Room ensures that you're not doing any long running operations on
     * the main thread, blocking the UI, so we don't need to handle changing Dispatchers.
     * ViewModels have a coroutine scope based on their lifecycle called
     * viewModelScope which we can use here.
     */
    fun insert(dbItem: DbItem) = viewModelScope.launch {
        repo.insert(dbItem)
    }
}