// Copyright (C) 2011 - Will Glozer.  All rights reserved.

package com.smttcn.commons.helpers

import java.util.Arrays
import kotlin.experimental.and

/**
 * High-performance base64 codec based on the algorithm used in Mikael Grev's MiG Base64.
 * This implementation is designed to handle base64 without line splitting and with
 * optional padding. Alternative character tables may be supplied to the `encode`
 * and `decode` methods to implement modified base64 schemes.
 *
 * Decoding assumes correct input, the caller is responsible for ensuring that the input
 * contains no invalid characters.
 *
 * @author Will Glozer
 */
object Base64 {
    private val encode =
        "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/".toCharArray()
    private val decode = IntArray(128)
    private val pad = '='

    init {
        Arrays.fill(decode, -1)
        for (i in encode.indices) {
            decode[encode[i].toInt()] = i
        }
        decode[pad.toInt()] = 0
    }

    /**
     * Decode base64 chars to bytes.
     *
     * @param chars Chars to encode.
     *
     * @return Decoded bytes.
     */
    fun decode(chars: CharArray): ByteArray {
        return decode(chars, decode, pad)
    }

    /**
     * Encode bytes to base64 chars, with padding.
     *
     * @param bytes Bytes to encode.
     *
     * @return Encoded chars.
     */
    fun encode(bytes: ByteArray): CharArray {
        return encode(bytes, encode, pad)
    }

    /**
     * Encode bytes to base64 chars, with optional padding.
     *
     * @param bytes     Bytes to encode.
     * @param padded    Add padding to output.
     *
     * @return Encoded chars.
     */
    fun encode(bytes: ByteArray, padded: Boolean): CharArray {
        return encode(bytes, encode, if (padded) pad else 0.toChar())
    }

    /**
     * Decode base64 chars to bytes using the supplied decode table and padding
     * character.
     *
     * @param src   Base64 encoded data.
     * @param table Decode table.
     * @param pad   Padding character.
     *
     * @return Decoded bytes.
     */
    fun decode(src: CharArray, table: IntArray, pad: Char): ByteArray {
        val len = src.size

        if (len == 0) return ByteArray(0)

        val padCount = if (src[len - 1] == pad) if (src[len - 2] == pad) 2 else 1 else 0
        val bytes = (len * 6 shr 3) - padCount
        val blocks = bytes / 3 * 3

        val dst = ByteArray(bytes)
        var si = 0
        var di = 0

        while (di < blocks) {
            val n =
                table[src[si++].toInt()] shl 18 or (table[src[si++].toInt()] shl 12) or (table[src[si++].toInt()] shl 6) or table[src[si++].toInt()]
            dst[di++] = (n shr 16).toByte()
            dst[di++] = (n shr 8).toByte()
            dst[di++] = n.toByte()
        }

        if (di < bytes) {
            var n = 0
            when (len - si) {
                4 -> {
                    n = n or table[src[si + 3].toInt()]
                    n = n or (table[src[si + 2].toInt()] shl 6)
                    n = n or (table[src[si + 1].toInt()] shl 12)
                    n = n or (table[src[si].toInt()] shl 18)
                }
                3 -> {
                    n = n or (table[src[si + 2].toInt()] shl 6)
                    n = n or (table[src[si + 1].toInt()] shl 12)
                    n = n or (table[src[si].toInt()] shl 18)
                }
                2 -> {
                    n = n or (table[src[si + 1].toInt()] shl 12)
                    n = n or (table[src[si].toInt()] shl 18)
                }
                1 -> n = n or (table[src[si].toInt()] shl 18)
            }
            var r = 16
            while (di < bytes) {
                dst[di++] = (n shr r).toByte()
                r -= 8
            }
        }

        return dst
    }

    /**
     * Encode bytes to base64 chars using the supplied encode table and with
     * optional padding.
     *
     * @param src   Bytes to encode.
     * @param table Encoding table.
     * @param pad   Padding character, or 0 for no padding.
     *
     * @return Encoded chars.
     */
    fun encode(src: ByteArray, table: CharArray, pad: Char): CharArray {
        val len = src.size

        if (len == 0) return CharArray(0)

        val blocks = len / 3 * 3
        var chars = (len - 1) / 3 + 1 shl 2
        val tail = len - blocks
        if (pad.toInt() == 0 && tail > 0) chars -= 3 - tail

        val dst = CharArray(chars)
        var si = 0
        var di = 0

        while (si < blocks) {
            val n : Int = src[si++].toInt().and(0xff).shl(16) or
                    src[si++].toInt().and(0xff).shl(8) or
                    src[si++].toInt().and(0xff)
            dst[di++] = table[n.ushr(18) and 0x3f]
            dst[di++] = table[n.ushr(12) and 0x3f]
            dst[di++] = table[n.ushr(6) and 0x3f]
            dst[di++] = table[n and 0x3f]
        }

        if (tail > 0) {
            var n = src[si].toInt().and(0xff).shl(10)
            if (tail == 2) n = n or src[++si].toInt().and(0xff).shl(2)

            dst[di++] = table[n.ushr(12) and 0x3f]
            dst[di++] = table[n.ushr(6) and 0x3f]
            if (tail == 2) dst[di++] = table[n and 0x3f]

            if (pad.toInt() != 0) {
                if (tail == 1) dst[di++] = pad
                dst[di] = pad
            }
        }

        return dst
    }
}