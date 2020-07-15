package com.smttcn.commons.models

import android.graphics.drawable.Drawable
import com.google.gson.Gson
import com.smttcn.commons.extensions.*
import com.smttcn.commons.helpers.*
import com.smttcn.safebox.MyApplication
import java.io.File
import java.util.*


open class FileDirItem(_file: File) : Comparable<FileDirItem> {

    companion object {
        var sorting = 0
    }

    var path: String = ""
    var filename: String = ""
    var isDirectory: Boolean = false
    var children: Int = 0
    var size: Long = 0L
    var modified: Long = 0L
    var mimeType: String = ""
    var fileIntentLabel: String = ""

    var isSelected: Boolean = false
    var isOptionMenuActive: Boolean = false

    init {
        path = _file.canonicalPath
        filename = _file.name
        isDirectory = _file.isDirectory
        children = 0
        size = _file.length()
        modified = _file.lastModified()
        mimeType = _file.getMimeType()

        isSelected = false
        isOptionMenuActive = false

    }

    override fun toString() = "FileDirItem(fullPathWithFilename=$path, filename=$filename, isDirectory=$isDirectory, children=$children, size=$size, modified=$modified)"

    override fun compareTo(other: FileDirItem): Int {
        return if (isDirectory && !other.isDirectory) {
            -1
        } else if (!isDirectory && other.isDirectory) {
            1
        } else {
            var result: Int
            when {
                sorting and SORT_BY_NAME != 0 -> result = filename.lowerCase().compareTo(other.filename.lowerCase())
                sorting and SORT_BY_SIZE != 0 -> result = when {
                    size == other.size -> 0
                    size > other.size -> 1
                    else -> -1
                }
                sorting and SORT_BY_DATE_MODIFIED != 0 -> {
                    result = when {
                        modified == other.modified -> 0
                        modified > other.modified -> 1
                        else -> -1
                    }
                }
                sorting and SORT_BY_FOLDERNAME != 0 -> result = when {
                    // sort by folder/fullPathWithFilename name
                    getParentPath() == other.getParentPath() -> 0
                    getParentPath() > other.getParentPath() -> 1
                    else -> -1

                }
                else -> {
                    result = getExt().lowerCase().compareTo(other.getExt().lowerCase())
                }
            }

            if (sorting and SORT_DESCENDING != 0) {
                result *= -1
            }
            result
        }
    }

    fun getThumbnailDrawable(): Drawable? {
        var file = File(path)
        val drawable = MyApplication.mainActivityContext.getDrawable(file.getFileTypeDrawableId())
        return drawable
    }
    fun getOriginalFilename(): String {
        return filename.removeSuffix("." + ENCRYPTED_FILE_EXT)
    }

    fun getExt(): String {
        if (!isDirectory)
            return filename.getFileExtension()
        else
            return ""
    }

    fun getProperSize(countHidden: Boolean) = File(path).getProperSize(countHidden)

    fun getProperFileCount(countHidden: Boolean) = File(path).getFileCount(countHidden)

    fun getDirectChildrenCount(countHiddenItems: Boolean) = File(path).getDirectChildrenCount(countHiddenItems)

    fun getParentPath() = path.getParentPath()

    fun deepcopy(): FileDirItem {
        val JSON = Gson().toJson(this)
        return Gson().fromJson(JSON, FileDirItem::class.java)
    }

}
