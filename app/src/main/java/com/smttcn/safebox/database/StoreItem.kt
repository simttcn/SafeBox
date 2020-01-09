package com.smttcn.safebox.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import com.smttcn.commons.extensions.withTrailingCharacter

@Entity(tableName = "storeItem")
data class StoreItem(
    @PrimaryKey(autoGenerate = true) val uid: Int = 0,
    @ColumnInfo(name = "filename") val fileName: String,
    @ColumnInfo(name = "hashed_filename") val hashedFileName: String,
    @ColumnInfo(name = "is_folder") val isFolder: Boolean,
    @ColumnInfo(name = "path") val path: String,
    @ColumnInfo(name = "salt") val salt: String,
    @ColumnInfo(name = "size") val size: Long
) {
    fun nameWithPath(): String {
        return path.withTrailingCharacter('/') + fileName
    }
}
