package com.smttcn.safebox.database

import androidx.lifecycle.LiveData

// Declares the DAO as a private property in the constructor. Pass in the DAO
// instead of the whole database, because you only need access to the DAO
class DbItemRepository(private val dbItemDao: DbItemDao) {

    // Room executes all queries on a separate thread.
    // Observed LiveData will notify the observer when the data has changed.
    val allDbItems: LiveData<List<DbItem>> = dbItemDao.getAll()

    suspend fun getAllDbItems() : List<DbItem> {
        return dbItemDao.getAllDbItems()
    }

    suspend fun getAllHashedFilename() : List<String> {
        return dbItemDao.getAllDbItemHashedFilenames()
    }

    suspend fun insert(dbItem: DbItem) {
        dbItemDao.insert(dbItem)
    }

    suspend fun delete(dbItem: DbItem) {
        dbItemDao.delete(dbItem)
    }
}