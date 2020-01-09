package com.smttcn.safebox.Manager

import androidx.room.Room
import com.smttcn.commons.Manager.FileManager
import com.smttcn.safebox.MyApplication
import com.smttcn.safebox.database.AppDatabase
import com.smttcn.safebox.database.StoreItem

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

    fun Initialize() : Boolean{
        RefreshDatabase()
        return true
    }

    fun getDb() = db

    fun RefreshDatabase() {
        AddNewItems()
        RemoveObsoleteItems()
    }

    fun RemoveObsoleteItems() {
        val allItems = db.storeItemDao().getAll()
        for(item in allItems) {
            if (item.isFolder) {
                if (!FileManager.IsFolderExist(item.NameWithPath())) {
                    // physical item not exist, remove the table entry
                    db.storeItemDao().delete(item)
                }
            } else {
                if (!FileManager.IsFileExist(item.NameWithPath())) {
                    // physical item not exist, remove the table entry
                    db.storeItemDao().delete(item)
                }
            }

        }
    }

    fun AddNewItems() {
        // Todo: 20200107 - should get files from all subfolder
        val newItems = FileManager.GetFilesInFolder(includeSubfolder = true)
        for(item in newItems) {
            if (db.storeItemDao().findByFileName(item.name).isEmpty()) {
                db.storeItemDao().insertAll(
                    StoreItem(
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
                )
            }
        }
    }
}