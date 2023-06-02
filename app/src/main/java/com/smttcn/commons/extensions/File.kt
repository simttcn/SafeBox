package com.smttcn.commons.extensions

import com.smttcn.commons.helpers.*
import com.smttcn.commons.models.FileDirItem
import com.smttcn.safebox.R
import java.io.File
import java.util.*

fun File.getMimeType() = absolutePath.getMimeType()

fun File.getProperSize(countHiddenItems: Boolean): Long {
    return if (isDirectory) {
        getDirectorySize(this, countHiddenItems)
    } else {
        length()
    }
}

private fun getDirectorySize(dir: File, countHiddenItems: Boolean): Long {
    var size = 0L
    if (dir.exists()) {
        val files = dir.listFiles()
        if (files != null) {
            for (i in files.indices) {
                if (files[i].isDirectory) {
                    size += getDirectorySize(files[i], countHiddenItems)
                } else if (!files[i].isHidden && !dir.isHidden || countHiddenItems) {
                    size += files[i].length()
                }
            }
        }
    }
    return size
}

fun File.getFileCount(countHiddenItems: Boolean, includeDirectory: Boolean = true): Int {
    return if (isDirectory) {
        getDirectoryFileCount(this, countHiddenItems, includeDirectory)
    } else {
        1
    }
}

private fun getDirectoryFileCount(dir: File, countHiddenItems: Boolean, includeDirectory: Boolean = true): Int {
    var count = -1
    if (dir.exists()) {
        val files = dir.listFiles()
        if (files != null) {
            count++
            for (i in files.indices) {
                val file = files[i]
                if (file.isDirectory) {
                    if (!file.isHidden || countHiddenItems) {
                        if (includeDirectory) count++
                        count += getDirectoryFileCount(file, countHiddenItems, includeDirectory)
                    }
                } else if (!file.isHidden || countHiddenItems) {
                    count++
                }
            }
        }
    }
    return count
}

fun File.getDirectChildrenCount(countHiddenItems: Boolean) = listFiles()?.filter { if (countHiddenItems) true else !it.isHidden }?.size ?: 0

fun File.toFileDirItem() = FileDirItem(this)

fun File.copyToFolder(targetFolder: File): File {
    val targetFile = File(targetFolder.canonicalPath.appendPath(this.name))
    return this.copyTo(targetFile, overwrite = true)
}

fun File.containsNoMedia() = isDirectory && File(this, NOMEDIA).exists()

fun File.doesThisOrParentHaveNoMedia(): Boolean {
    var curFile = this
    while (true) {
        if (curFile.containsNoMedia()) {
            return true
        }
        curFile = curFile.parentFile ?: break
        if (curFile.absolutePath == "/") {
            break
        }
    }
    return false
}

fun File.getFileTypeDrawableId(): Int {
    //val ext = "." + FileManager.getFileExtensionFromEncryptedFile(this)
    val ext = "." + this.name.removeEncryptedExtension().getFileExtension().lowercase(Locale.getDefault())

    if (photoExtensions.contains(ext)) return R.drawable.fileicon_image_50
    if (videoExtensions.contains(ext)) return R.drawable.fileicon_video_50
    if (audioExtensions.contains(ext)) return R.drawable.fileicon_audio_50
    if (rawExtensions.contains(ext)) return R.drawable.fileicon_raw_50
    if (officeWorldExtensions.contains(ext)) return R.drawable.fileicon_document
    if (officeExcelExtensions.contains(ext)) return R.drawable.fileicon_document
    if (officePowerPointExtensions.contains(ext)) return R.drawable.fileicon_document
    if (PdfExtensions.contains(ext)) return R.drawable.fileicon_pdf_50
    if (TextExtensions.contains(ext)) return R.drawable.fileicon_txt_50
    if (archiveExtensions.contains(ext)) return R.drawable.fileicon_archive_50

    return R.drawable.fileicon_document
}
