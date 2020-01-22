package com.smttcn.safebox.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.smttcn.commons.Manager.FileManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.*
import net.sqlcipher.database.SQLiteDatabase
import net.sqlcipher.database.SupportFactory

@Database(entities = arrayOf(DbItem::class), version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {

    abstract fun dbItemDao(): DbItemDao

    private class AppDatabaseCallback(
        private val scope: CoroutineScope
    ) : RoomDatabase.Callback() {

        override fun onOpen(db: SupportSQLiteDatabase) {
            database = db
            super.onOpen(db)
            INSTANCE?.let { database ->
                scope.launch {
                    refresh(database.dbItemDao())
                }
            }
        }
    }

    companion object : CoroutineScope by MainScope() {
        // Singleton prevents multiple instances of database opening at the
        // same time.
        @Volatile
        private var INSTANCE: AppDatabase? = null
        private val dbName = "SafeBoxDB.data"
        private var supportFactory : SupportFactory? = null
        private var database :  SupportSQLiteDatabase? = null

        fun setKey(secretKey: String) {
            supportFactory = SupportFactory(secretKey.toByteArray())
        }

        fun reKey(secretKey: String) {
            database?.query("PRAGMA rekey='" + secretKey + "'")
            database?.close()
            INSTANCE?.close()
            INSTANCE = null
        }

        fun getDb(appContext: Context, scope: CoroutineScope): AppDatabase? {
            return INSTANCE ?: synchronized(this) {
                if (supportFactory != null) {
                    val instance = Room.databaseBuilder(
                        appContext,
                        AppDatabase::class.java,
                        dbName
                    ).openHelperFactory(
                        supportFactory
                    ).addCallback(
                        AppDatabaseCallback(scope)
                    ).build()
                    INSTANCE = instance

                    instance
                } else{
                    null
                }
            }
        }

        suspend fun refresh(dbItemDao: DbItemDao) {
            removeObsoleteItems(dbItemDao)
            addNewItems(dbItemDao)
        }

        suspend private fun removeObsoleteItems(dbItemDao: DbItemDao) {
            // get all file info from the database
            val allDbItems = dbItemDao.getAllDbItems()
            for (item in allDbItems) { // loop them through
                if (item.isFolder) {
                    // it's a folder, so does it exist?
                    if (!FileManager.isFolderExist(item.hashedFilenameWithPath())) {
                        // physical item not exist, remove the table entry
                        dbItemDao.delete(item)
                    }
                } else {
                    // it's a file, so does it exist?
                    if (!FileManager.isFileExist(item.hashedFilenameWithPath())) {
                        // physical item not exist, remove the table entry
                        dbItemDao.delete(item)
                    }
                }

            }
        }

        suspend private fun addNewItems(dbItemDao: DbItemDao) {
            // get all files within the data folder
            val newItems = FileManager.getFilesInFolder(includeSubfolder = true)
            // get all existing hashed filename from the database in a temp array
            val allDbItems = dbItemDao.getAllDbItemHashedFilenames()
            // loop through all the files we found in the data folders
            for (item in newItems) {
                // see if it not already exist in the database by checking the temp array
                if (!allDbItems.contains(item.name)) {
                    // not found in the array, i.e. the database, so create an object for inserting
                    val dbItem = DbItem(
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
                    // add the file info into the database
                    dbItemDao.insert(dbItem)
                }
            }
        }


    }
}