package com.smttcn.safebox.database

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface DbItemDao {
    @Query("SELECT * FROM dbItem")
    fun getAll(): LiveData<List<DbItem>>

    @Query("SELECT hashed_filename FROM dbItem")
    suspend fun getAllDbItemHashedFilenames(): List<String>

    @Query("SELECT * FROM dbItem")
    suspend fun getAllDbItems(): List<DbItem>

    @Query("SELECT * FROM dbItem WHERE hashed_filename LIKE :hashedFilename LIMIT 1")
    suspend fun findByHashedFileName(hashedFilename: String): List<DbItem>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(dbItem: DbItem)

    @Delete
    suspend fun delete(dbItem: DbItem)

    @Query("DELETE FROM dbItem WHERE hashed_filename = :hashedFilename")
    suspend fun deleteByHashedFileName(hashedFilename: String) : Int

}