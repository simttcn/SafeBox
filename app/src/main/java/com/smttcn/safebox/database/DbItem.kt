package com.smttcn.safebox.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.smttcn.commons.extensions.getParentPath
import com.smttcn.commons.extensions.withTrailingCharacter

@Entity(tableName = "dbItem")
data class DbItem(
    @PrimaryKey(autoGenerate = true) val uid: Int = 0,
    @ColumnInfo(name = "filename") val fileName: String,
    @ColumnInfo(name = "hashed_filename") val hashedFileName: String,
    @ColumnInfo(name = "is_folder") val isFolder: Boolean,
    @ColumnInfo(name = "full_path_with_filename") val fullPathWithFilename: String,
    @ColumnInfo(name = "salt") val salt: String,
    @ColumnInfo(name = "size") val size: Long
) {
    fun pathOnly(): String {
        return fullPathWithFilename.getParentPath()
    }
}
