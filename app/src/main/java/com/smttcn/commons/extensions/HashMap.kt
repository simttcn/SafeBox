package com.smttcn.commons.extensions

import com.smttcn.crypto.Base64

private const val delimiter0 = "$=//=$"
private const val delimiter1 = "$=\\=$"

fun HashMap<String, ByteArray>.toBase64String() : String {
    var sb = StringBuilder()
    for(hash in this) {
        sb.append(delimiter0 + hash.key + delimiter1 + String(Base64.encode(hash.value)))
    }
    return sb.toString()
}

fun HashMap<String, ByteArray>.fromBase64String(str : String) : HashMap<String, ByteArray> {

    var strArray = str.toString().split(delimiter0).dropWhile { it.isEmpty() }
    val map = java.util.HashMap<String, ByteArray>()
    for (s in strArray) {
        val array1 = s.split(delimiter1)
        map[array1[0]] = Base64.decode(array1[1].toCharArray())
    }
    return map
}