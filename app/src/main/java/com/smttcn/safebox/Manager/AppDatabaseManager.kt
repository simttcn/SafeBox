package com.smttcn.safebox.Manager

import androidx.room.Room
import com.smttcn.commons.Manager.FileManager
import com.smttcn.safebox.MyApplication
import com.smttcn.safebox.database.AppDatabase
import com.smttcn.safebox.database.StoreItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object AppDatabaseManager {

    private val dbName = "SafeBoxDB.data"
    private val db : AppDatabase

    init {
        db = Room.databaseBuilder(
            MyApplication.getAppContext(),
            AppDatabase::class.java,
            dbName
        ).build()
    }

    suspend fun initialize() : Boolean{
        refreshDatabase()
        return true
    }

    fun getDb() = db

    suspend fun refreshDatabase() {
        addNewItems()
        removeObsoleteItems()
    }

    private suspend fun removeObsoleteItems() {
        withContext(Dispatchers.Default) {
            val allItems = db.storeItemDao().getAll()
            for (item in allItems) {
                if (item.isFolder) {
                    if (!FileManager.isFolderExist(item.nameWithPath())) {
                        // physical item not exist, remove the table entry
                        db.storeItemDao().delete(item)
                    }
                } else {
                    if (!FileManager.isFileExist(item.nameWithPath())) {
                        // physical item not exist, remove the table entry
                        db.storeItemDao().delete(item)
                    }
                }

            }
        }
    }

    private suspend fun addNewItems() {
        // Get files from all subfolder
        withContext(Dispatchers.Default) {
            val newItems = FileManager.getFilesInFolder(includeSubfolder = true)
            for (item in newItems) {
                if (StoreItemManager.isStoreItemExist(item.name)) {
                    val storeItem = StoreItem(
                        fileName = item.name,
                        hashedFileName = "",
                        isFolder = item.isDirectory,
                        path = item.path.replace(FileManager.documentRoot, "").replace(
                            item.name,
                            ""
                        ),
                        salt = "",
                        size = item.length()
                    )
                    StoreItemManager.insert(storeItem)
                }
            }
        }
    }
}