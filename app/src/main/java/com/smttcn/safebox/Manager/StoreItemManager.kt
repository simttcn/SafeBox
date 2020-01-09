package com.smttcn.safebox.Manager

import com.smttcn.commons.Manager.FileManager
import com.smttcn.commons.models.FileDirItem
import com.smttcn.safebox.database.StoreItem

object StoreItemManager {

    var itemList: List<StoreItem>

    init {
        itemList = AppDatabaseManager.getDb().storeItemDao().getAll()
    }

    fun GetItemList(): List<StoreItem>{
        if (itemList.size > 0)
            return itemList
        else
            return emptyList()
    }
}