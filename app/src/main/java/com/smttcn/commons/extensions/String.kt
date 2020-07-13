package com.smttcn.commons.extensions

import android.graphics.Bitmap
import android.os.StatFs
import android.text.Spannable
import android.text.SpannableString
import android.text.TextUtils
import android.text.style.ForegroundColorSpan
import com.smttcn.commons.helpers.*
import java.io.File
import java.text.Normalizer
import java.util.*
import java.util.regex.Pattern

fun String.addExtension(ext: String) = this + "." + ext

fun String.appendPath(path: String) = this.removeSuffix("/") + "/" + path

fun String.removeTrailingCharacter(character: Char): String {
    if (endsWith(character, ignoreCase = false)) {
        return substring(0, length - 1)
    } else return this
}

fun String.withTrailingCharacter(character: Char): String {
    if (!endsWith(character, ignoreCase = false)) {
        return this + character
    } else return this
}

fun String.removeEncryptedExtension() = removeSuffix("." + ENCRYPTED_FILE_EXT)


fun String.getFilenameFromPath() : String {
    if (lastIndexOf("/") < 1)
        return this
    else
        return substring(lastIndexOf("/") + 1)
}

fun String.getUnencryptedFilenameFromPath() : String {
    if (lastIndexOf("/") < 1)
        return this
    else
        return substring(lastIndexOf("/") + 1).removeEncryptedExtension()
}

fun String.getFileExtension() = substring(lastIndexOf(".") + 1)

fun String.insertBeforeFileExtension(str: String): String {
    var ext = this.getFileExtension()
    return this.removeSuffix("." + ext) + str + "." + ext
}

fun String.isAValidFilename(): Boolean {
    val ILLEGAL_CHARACTERS = charArrayOf('/', '\n', '\r', '\t', '\u0000', '`', '?', '*', '\\', '<', '>', '|', '\"', ':')
    ILLEGAL_CHARACTERS.forEach {
        if (contains(it))
            return false
    }
    return true
}

fun String.isMediaFile() = isImageExtension() || isVideoExtension() || isGif() || isRawExtension() || isSvg()

fun String.isGif() = endsWith(".gif", true)

fun String.isPng() = endsWith(".png", true)

fun String.isJpg() = endsWith(".jpg", true) or endsWith(".jpeg", true)

fun String.isSvg() = endsWith(".svg", true)

// fast extension checks, not guaranteed to be accurate
fun String.isImageExtension() = photoExtensions.any { endsWith(it, true) }
fun String.isAudioExtension() = audioExtensions.any { endsWith(it, true) }
fun String.isVideoExtension() = videoExtensions.any { endsWith(it, true) }
fun String.isRawExtension() = rawExtensions.any { endsWith(it, true) }

fun String.isImageMimeType() = isImageExtension() || getMimeType().startsWith("image")
fun String.isVideoMimeType() = isVideoExtension() || getMimeType().startsWith("video")
fun String.isAudioMimeType() = isAudioExtension() || getMimeType().startsWith("audio")

fun String.getCompressionFormat() = when (getFileExtension().toLowerCase()) {
    "png" -> Bitmap.CompressFormat.PNG
    "webp" -> Bitmap.CompressFormat.WEBP
    else -> Bitmap.CompressFormat.JPEG
}

fun String.areDigitsOnly() = matches(Regex("[0-9]+"))

fun String.getGenericMimeType(): String {
    if (!contains("/"))
        return this

    val type = substring(0, indexOf("/"))
    return "$type/*"
}

fun String.getParentPath() = removeSuffix("/${getFilenameFromPath()}")

fun String.substringTo(cnt: Int): String {
    return if (isEmpty()) {
        ""
    } else {
        substring(0, Math.min(length, cnt))
    }
}

fun String.highlightTextPart(textToHighlight: String, color: Int, highlightAll: Boolean = false, ignoreCharsBetweenDigits: Boolean = false): SpannableString {
    val spannableString = SpannableString(this)
    if (textToHighlight.isEmpty()) {
        return spannableString
    }

    var startIndex = normalizeString().indexOf(textToHighlight, 0, true)
    val indexes = ArrayList<Int>()
    while (startIndex >= 0) {
        if (startIndex != -1) {
            indexes.add(startIndex)
        }

        startIndex = normalizeString().indexOf(textToHighlight, startIndex + textToHighlight.length, true)
        if (!highlightAll) {
            break
        }
    }

    // handle cases when we search for 643, but in reality the string contains it like 6-43
    if (ignoreCharsBetweenDigits && indexes.isEmpty()) {
        try {
            val regex = TextUtils.join("(\\D*)", textToHighlight.toCharArray().toTypedArray())
            val pattern = Pattern.compile(regex)
            val result = pattern.matcher(normalizeString())
            if (result.find()) {
                spannableString.setSpan(ForegroundColorSpan(color), result.start(), result.end(), Spannable.SPAN_EXCLUSIVE_INCLUSIVE)
            }
        } catch (ignored: Exception) {
        }

        return spannableString
    }

    indexes.forEach {
        val endIndex = Math.min(it + textToHighlight.length, length)
        try {
            spannableString.setSpan(ForegroundColorSpan(color), it, endIndex, Spannable.SPAN_EXCLUSIVE_INCLUSIVE)
        } catch (ignored: IndexOutOfBoundsException) {
        }
    }

    return spannableString
}

fun String.getFileKey(): String {
    val file = File(this)
    return "${file.absolutePath}${file.lastModified()}"
}

fun String.getAvailableStorageB(): Long {
    val stat = StatFs(this)
    val bytesAvailable = stat.blockSizeLong * stat.availableBlocksLong
    return bytesAvailable
}

// remove diacritics, for example č -> c
fun String.normalizeString() = Normalizer.normalize(this, Normalizer.Form.NFD).replace(normalizeRegex, "")

fun String.getMimeType(): String {
    return MimeType.typesMap[getFileExtension().toLowerCase()] ?: ""
}
