package com.smttcn.safebox.Manager

import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.smttcn.commons.Manager.FileManager
import com.smttcn.safebox.MyApplication
import com.smttcn.safebox.database.AppDatabase
import com.smttcn.safebox.database.StoreItem
import kotlinx.coroutines.*

object AppDatabaseManager : CoroutineScope by MainScope() {

    private val dbName = "SafeBoxDB.data"
    lateinit private var db : AppDatabase

    var isReady : Boolean

    init {
        isReady = false
    }

    fun initialize() : Boolean{
        db = Room.databaseBuilder(
            MyApplication.getAppContext(),
            AppDatabase::class.java,
            dbName
        ).build()

        refreshDatabase()
        return true
    }

    fun getDb() = db

    fun refreshDatabase() {
        launch {
            isReady = false
            addNewItems()
            removeObsoleteItems()
            isReady = true
        }
    }

    private suspend fun removeObsoleteItems() {
        withContext(Dispatchers.Default) {
            val allItems = db.storeItemDao().getAll()
            for (item in allItems) {
                if (item.isFolder) {
                    if (!FileManager.isFolderExist(item.hashedFilenameWithPath())) {
                        // physical item not exist, remove the table entry
                        db.storeItemDao().delete(item)
                    }
                } else {
                    if (!FileManager.isFileExist(item.hashedFilenameWithPath())) {
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
                        hashedFileName = item.name,
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