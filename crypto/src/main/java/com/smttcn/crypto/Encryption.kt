/*
 * Copyright (c) 2019 Razeware LLC
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * Notwithstanding the foregoing, you may not use, copy, modify, merge, publish,
 * distribute, sublicense, create a derivative work, and/or sell copies of the
 * Software in any work that is designed, intended, or marketed for pedagogical or
 * instructional purposes related to programming, coding, application development,
 * or information technology.  Permission for such use, copying, modification,
 * merger, publication, distribution, sublicensing, creation of derivative works,
 * or sale is expressly withheld.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package com.smttcn.crypto

import android.util.Log
import java.security.SecureRandom
import java.util.*
import javax.crypto.Cipher
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec

public class Encryption {

    fun generateSecret(saltLen : Int = 64) : String {
        val random = SecureRandom()
        val pwd = ByteArray(saltLen)
        random.nextBytes(pwd)
        return String(Base64.encode(pwd)).dropLast(2)
    }

    fun encryptWithFilename(filename: String, dataToEncrypt: ByteArray, password: CharArray): HashMap<String, ByteArray> {

        var map = encrypt(dataToEncrypt, password)
        map[APP_ENCRYPT_VAR0] = filename.toByteArray()

        return map
    }

    fun encrypt(dataToEncrypt: ByteArray, password: CharArray): HashMap<String, ByteArray> {
        val map = HashMap<String, ByteArray>()
        try {
            //Random salt for next step
            val random = SecureRandom()
            val salt = ByteArray(256)
            random.nextBytes(salt)

            val cipher = getCipher(Cipher.ENCRYPT_MODE, password, salt, null)

            //Encrypt
            val encrypted = cipher.doFinal(dataToEncrypt)

            map[APP_ENCRYPT_VAR2] = salt
            map[APP_ENCRYPT_VAR1] = cipher.iv
            map[APP_ENCRYPT_VAR3] = encrypted
        } catch (e: Exception) {
            Log.e("MYAPP", "encryption exception", e)
        }

        return map

    }

    fun decrypt(map: HashMap<String, ByteArray>, password: CharArray): ByteArray? {
        var decrypted: ByteArray? = null
        try {
            val iv = map[APP_ENCRYPT_VAR1]
            val salt = map[APP_ENCRYPT_VAR2]
            val encrypted = map[APP_ENCRYPT_VAR3]

            val cipher = getCipher(Cipher.DECRYPT_MODE, password, salt, iv)

            //Decrypt
            decrypted = cipher.doFinal(encrypted)
        } catch (e: Exception) {
            Log.e("MYAPP", "decryption exception", e)
        }

        return decrypted
    }

    fun decryptToHashMap(map: HashMap<String, ByteArray>, password: CharArray): HashMap<String, ByteArray> {
        val result = HashMap<String, ByteArray>()
        try {
            val iv = map[APP_ENCRYPT_VAR1]
            val salt = map[APP_ENCRYPT_VAR2]
            val encrypted = map[APP_ENCRYPT_VAR3]

            if (salt != null && iv != null && encrypted != null) {
                val cipher = getCipher(Cipher.DECRYPT_MODE, password, salt, iv)

                //Decrypt
                result[APP_ENCRYPT_VAR1] = cipher.iv
                result[APP_ENCRYPT_VAR2] = salt
                result[APP_ENCRYPT_VAR3] = cipher.doFinal(encrypted)
            }
        } catch (e: Exception) {
            Log.e("MYAPP", "decryption exception", e)
        }

        return result
    }

    private fun getCipher(mode: Int, password: CharArray, s: ByteArray?, i: ByteArray?) : Cipher {

        var salt = s
        var iv = i

        if (mode == Cipher.ENCRYPT_MODE) {
            //Create initialization vector for AES
            val ivRandom = SecureRandom() //not caching previous seeded instance of SecureRandom
            iv = ByteArray(16)
            ivRandom.nextBytes(iv)
        }

        val pbKeySpec = PBEKeySpec(password, salt, KEY_HASH_ITERATION_COUNT, 256)
        val secretKeyFactory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1")
        val keyBytes = secretKeyFactory.generateSecret(pbKeySpec).encoded
        val keySpec = SecretKeySpec(keyBytes, "AES")

        val ivSpec = IvParameterSpec(iv)

        val cipher = Cipher.getInstance("AES/CBC/PKCS7Padding")
        cipher.init(mode, keySpec, ivSpec)

        return cipher
    }

}

