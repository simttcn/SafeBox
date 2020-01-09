package com.smttcn.safebox.Manager

import com.smttcn.safebox.database.StoreItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object StoreItemManager {

    private var itemList: List<StoreItem>

    init {
        itemList = AppDatabaseManager.getDb().storeItemDao().getAll()
    }

    fun initialize() : Boolean{
        return true
    }

    fun getItemList(): List<StoreItem>{
        if (itemList.size > 0)
            return itemList
        else
            return emptyList()
    }

    suspend fun refreshItemList() {
        withContext(Dispatchers.Default) {
            itemList = AppDatabaseManager.getDb().storeItemDao().getAll()
        }
    }

    suspend fun isStoreItemExist(hashedName: String): Boolean {
        return withContext(Dispatchers.Default) {
            // Todo: check if StoreItem exist in database
            return@withContext AppDatabaseManager.getDb().storeItemDao().findByHashedFileName(hashedName).isEmpty()
        }
    }

    suspend fun insert(item: StoreItem) {
        withContext(Dispatchers.Default) {
            AppDatabaseManager.getDb().storeItemDao().insertAll(item)
        }
    }
}