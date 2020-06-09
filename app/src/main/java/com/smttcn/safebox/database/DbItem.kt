package com.smttcn.safebox.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.smttcn.commons.extensions.getParentPath
import com.smttcn.commons.extensions.withTrailingCharacter
import com.smttcn.commons.helpers.APP_ENCRYPT_VAR0
import com.smttcn.commons.helpers.APP_ENCRYPT_VAR2

@Entity(tableName = "dbItem")
data class DbItem(
    @PrimaryKey(autoGenerate = true) val uid: Int = 0,
    @ColumnInfo(name = APP_ENCRYPT_VAR0) val fileName: String,
    @ColumnInfo(name = "hashed_filename") val hashedFileName: String,
    @ColumnInfo(name = "is_folder") val isFolder: Boolean,
    @ColumnInfo(name = "full_path_with_filename") val fullPathWithFilename: String,
    @ColumnInfo(name = APP_ENCRYPT_VAR2) val salt: String,
    @ColumnInfo(name = "size") val size: Long,
    @ColumnInfo(name = "thumbnail", typeAffinity = ColumnInfo.BLOB) val thumbnail: ByteArray? = null
) {
    fun pathOnly(): String {
        return fullPathWithFilename.getParentPath()
    }
}
