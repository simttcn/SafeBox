// Copyright (C) 2011 - Will Glozer.  All rights reserved.

package com.smttcn.commons.crypto

import java.io.UnsupportedEncodingException
import java.security.GeneralSecurityException
import java.security.SecureRandom

import com.smttcn.commons.helpers.Base64.*
import kotlin.experimental.xor

/**
 * Simple [SCrypt] interface for hashing passwords using the
 * [scrypt](http://www.tarsnap.com/scrypt.html) key derivation function
 * and comparing a plain text password to a hashed one. The hashed output is an
 * extended implementation of the Modular Crypt Format that also includes the scrypt
 * algorithm parameters.
 *
 * Format: `$s0$PARAMS$SALT$KEY`.
 *
 * <dl>
 * <dd>PARAMS</dd><dt>32-bit hex integer containing log2(N) (16 bits), r (8 bits), and p (8 bits)</dt>
 * <dd>SALT</dd><dt>base64-encoded salt</dt>
 * <dd>KEY</dd><dt>base64-encoded derived key</dt>
</dl> *
 *
 * `s0` identifies version 0 of the scrypt format, using a 128-bit salt and 256-bit derived key.
 *
 * @author  Will Glozer
 */
object SCryptUtil {
    /**
     * Hash the supplied plaintext password and generate output in the format described
     * in [SCryptUtil].
     *
     * @param passwd            Password.
     * @param saltLength        Salt length.
     * @param keyLength         Key length.
     * @param cpuCost           CPU cost parameter.
     * @param memoryCost        Memory cost parameter.
     * @param Parallelization   Parallelization parameter.
     *
     * @return The hashed password.
     */
    fun scrypt(
        passwd: String,
        saltLength: Int,
        keyLength: Int,
        cpuCost: Int,
        memoryCost: Int,
        Parallelization: Int
    ): String {
        try {
            val salt = ByteArray(saltLength)
            val rnd = SecureRandom()
            rnd.nextBytes(salt)

            val derived = SCrypt.scrypt(
                passwd.toByteArray(charset("UTF-8")),
                salt,
                cpuCost,
                memoryCost,
                Parallelization,
                keyLength
            )

            val params = java.lang.Long.toString(
                (log2(cpuCost) shl 16 or (memoryCost shl 8) or Parallelization).toLong(),
                16
            )

            val sb = StringBuilder((salt.size + derived.size) * 2)
            sb.append("\$s0$").append(params).append('$')
            sb.append(encode(salt)).append('$')
            sb.append(encode(derived))

            return sb.toString()
        } catch (e: UnsupportedEncodingException) {
            throw IllegalStateException("JVM doesn't support UTF-8?")
        } catch (e: GeneralSecurityException) {
            throw IllegalStateException("JVM doesn't support SHA1PRNG or HMAC_SHA256?")
        }

    }

    /**
     * Compare the supplied plaintext password to a hashed password.
     *
     * @param   passwd      Plaintext password.
     * @param   hashed      scrypt hashed password.
     * @param   keyLength   Key length
     *
     * @return true if passwd matches hashed value.
     */
    fun check(passwd: String, hashed: String, keyLength: Int): Boolean {
        try {
            val parts = hashed.split("\\$".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()

            require(!(parts.size != 5 || parts[1] != "s0")) { "Invalid hashed value" }

            val params = java.lang.Long.parseLong(parts[2], 16)
            val salt = decode(parts[3].toCharArray())
            val derived0 = decode(parts[4].toCharArray())

            val N = Math.pow(2.0, (params shr 16 and 0xffff).toDouble()).toInt()
            val r = params.toInt() shr 8 and 0xff
            val p = params.toInt() and 0xff

            val derived1 =
                SCrypt.scrypt(passwd.toByteArray(charset("UTF-8")), salt, N, r, p, keyLength)

            if (derived0.size != derived1.size) return false

            var result = 0
            for (i in derived0.indices) {
                result = result or (derived0[i].xor(derived1[i])).toInt()
            }
            return result == 0
        } catch (e: UnsupportedEncodingException) {
            throw IllegalStateException("JVM doesn't support UTF-8?")
        } catch (e: GeneralSecurityException) {
            throw IllegalStateException("JVM doesn't support SHA1PRNG or HMAC_SHA256?")
        }

    }

    private fun log2(n: Int): Int {
        var n = n
        var log = 0
        if (n and -0x10000 != 0) {
            n = n ushr 16
            log = 16
        }
        if (n >= 256) {
            n = n ushr 8
            log += 8
        }
        if (n >= 16) {
            n = n ushr 4
            log += 4
        }
        if (n >= 4) {
            n = n ushr 2
            log += 2
        }
        return log + n.ushr(1)
    }
}
