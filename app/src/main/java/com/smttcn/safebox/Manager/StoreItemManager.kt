package com.smttcn.safebox.Manager

import com.smttcn.safebox.database.StoreItem
import kotlinx.coroutines.*

object StoreItemManager : CoroutineScope by MainScope() {

    lateinit private var itemList: List<StoreItem>

    init {}

    fun initialize() : Boolean{
        launch { refreshItemList() }
        return true
    }

    fun getItemList(): List<StoreItem>{
        if (itemList.size > 0)
            return itemList
        else
            return emptyList()
    }

    suspend fun refreshItemList(withTest : Boolean = false) {
        withContext(Dispatchers.Default) {
            // Todo: have to force Room to discard the cache and read the data from physical file
            if (withTest) {
                AppDatabaseManager.getDb().storeItemDao().deleteByHashedFileName("img17.jpg")
                AppDatabaseManager.getDb().storeItemDao().deleteByHashedFileName("img18.jpg")
            }
            itemList = AppDatabaseManager.getDb().storeItemDao().getAll()
        }
    }

    suspend fun isStoreItemExist(hashedName: String): Boolean {
        return withContext(Dispatchers.Default) {
            return@withContext AppDatabaseManager.getDb().storeItemDao().findByHashedFileName(hashedName).isEmpty()
        }
    }

    suspend fun insert(item: StoreItem) {
        withContext(Dispatchers.Default) {
            AppDatabaseManager.getDb().storeItemDao().insertAll(item)
        }
    }
}