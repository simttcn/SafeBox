package com.smttcn.safebox.database

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = arrayOf(StoreItem::class), version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun storeItemDao(): StoreItemDao
}