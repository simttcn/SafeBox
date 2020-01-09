package com.smttcn.safebox.database

import androidx.room.*

@Dao
interface StoreItemDao {
    @Query("SELECT * FROM storeItem")
    fun getAll(): List<StoreItem>

    @Query("SELECT * FROM storeItem WHERE uid IN (:storeItemIds)")
    fun loadAllByIds(storeItemIds: IntArray): List<StoreItem>

    @Query("SELECT * FROM storeItem WHERE filename LIKE :filename LIMIT 1")
    fun findByFileName(filename: String): List<StoreItem>

    @Query("SELECT * FROM storeItem WHERE hashed_filename LIKE :hashedFilename LIMIT 1")
    fun findByHashedFileName(hashedFilename: String): List<StoreItem>

    @Insert
    fun insertAll(vararg storeItems: StoreItem)

    @Delete
    fun delete(storeItem: StoreItem)
}