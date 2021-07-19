package com.smttcn.commons.extensions

import java.nio.ByteBuffer

fun ByteArray.fromInt(value: Int, len: Int) = {
    val bb = ByteBuffer.allocate(len)
    bb.putInt(value)
    bb.array()
}

