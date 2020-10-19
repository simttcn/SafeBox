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
import java.io.*
import java.security.SecureRandom
import java.util.*
import javax.crypto.Cipher
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec
import kotlin.ByteArray

public class Encryption {

    fun generateRandomByte(length : Int = 128) : ByteArray {
        val random = SecureRandom()
        val data = ByteArray(length)
        random.nextBytes(data)
        return data
    }

    fun generateSecretKey(length : Int = 128) : CharArray {
        return String(Base64.encode(generateRandomByte(length))).dropLast(2).toCharArray()
    }

    fun encryptWithFilename(filename: String, dataToEncrypt: ByteArray, password: CharArray): HashMap<String, ByteArray> {

        return encryptWithFilenameLite(filename,dataToEncrypt, password)
    }

    fun encryptWithFilenameLite(filename: String, dataToEncrypt: ByteArray, password: CharArray): HashMap<String, ByteArray> {

        var map = encrypt(dataToEncrypt, password)
        map[APP_ENCRYPT_VAR0] = filename.toByteArray()
        map[APP_ENCRYPT_VAR4] = generateRandomByte(128)

        return map
    }

    fun encryptWithFilenameHeavy(filename: String, dataToEncrypt: ByteArray, password: CharArray): HashMap<String, ByteArray> {

        // generate a secret key to encrypt the actual data
        var secretKey = this.generateSecretKey(256)

        // encrypt the actual data with auto generated secret key
        var mapData = encrypt(dataToEncrypt, secretKey)
        mapData[APP_ENCRYPT_VAR0] = generateRandomByte(128)
        mapData[APP_ENCRYPT_VAR4] = generateRandomByte(128)

        // then encrypt the secret key
        var map = encrypt(secretKey.toString().toByteArray(), password)

        map[APP_ENCRYPT_VAR0] = filename.toByteArray()
        map[APP_ENCRYPT_VAR4] = mapData.toByteArray()

        return map
    }

    fun decryptObjectInputStreamWithFilename(objectInputStream: ObjectInputStream, password: CharArray): Pair<String, ByteArray?> {

        return decryptObjectInputStreamLite(objectInputStream, password)

    }

    fun decryptObjectInputStreamLite(objectInputStream: ObjectInputStream, password: CharArray): Pair<String, ByteArray?> {

        val map = getEncryptedHashMap(objectInputStream.readObject())

        if (map != null) {

            try {
                val fn = map[APP_ENCRYPT_VAR0]
                val iv = map[APP_ENCRYPT_VAR1]
                val salt = map[APP_ENCRYPT_VAR2]
                val encrypted = map[APP_ENCRYPT_VAR3]

                if (fn is ByteArray && iv is ByteArray && salt is ByteArray && encrypted is ByteArray) {

                    //Decrypt
                    return Pair(String(fn), decrypt(iv, salt, encrypted, password))
                }
            } catch (e: Exception){
            }

        }

        return Pair("", null)

    }
    // todo: heavy decrypt function
    fun decryptObjectInputStreamHeavy(objectInputStream: ObjectInputStream, password: CharArray): Pair<String, ByteArray?> {

        val map = getEncryptedHashMap(objectInputStream.readObject())

        if (map != null) {

            try {
                val fn = map[APP_ENCRYPT_VAR0]
                val iv = map[APP_ENCRYPT_VAR1]
                val salt = map[APP_ENCRYPT_VAR2]
                val encryptedKey = map[APP_ENCRYPT_VAR3]
                val encryptedData = map[APP_ENCRYPT_VAR4]

                if (fn is ByteArray && iv is ByteArray && salt is ByteArray && encryptedData is ByteArray && encryptedKey is ByteArray) {

                    // 1. decrypt key
                    val decryptedKey = decrypt(iv, salt, encryptedKey, password)

                    if (decryptedKey != null) {

                        val key = String(decryptedKey, Charsets.UTF_8)
                        val mapData = getEncryptedHashMap(encryptedData)

                        // 2. decrypt data
                        if (mapData != null) {
                            val ivInner = mapData[APP_ENCRYPT_VAR1]
                            val saltInner = mapData[APP_ENCRYPT_VAR2]
                            val encryptedDataInner = mapData[APP_ENCRYPT_VAR3]

                            if (ivInner is ByteArray && saltInner is ByteArray && encryptedDataInner is ByteArray) {
                                return Pair(String(fn), decrypt(ivInner, saltInner, encryptedDataInner, key.toCharArray()))
                            }
                        }
                    }

                }
            } catch (e: Exception){
            }

        }

        return Pair("", null)

    }

    private fun encrypt(dataToEncrypt: ByteArray, password: CharArray): HashMap<String, ByteArray> {
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

    private fun decrypt(iv: ByteArray?, salt: ByteArray?, encrypted: ByteArray?, password: CharArray): ByteArray? {

        var decrypted: ByteArray? = null

        try {
            val cipher = getCipher(Cipher.DECRYPT_MODE, password, salt, iv)

            //Decrypt
            decrypted = cipher.doFinal(encrypted)

        } catch (e: Exception) {
            Log.e("MYAPP", "decryption exception", e)
        }

        return decrypted
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

    private fun getEncryptedHashMap(data: Any): HashMap<String, ByteArray>? {
        try {
            when (data) {
                is HashMap<*, *> -> {
                    @Suppress("UNCHECKED_CAST")
                    return data as? HashMap<String, ByteArray>
                }
            }
        } catch (ex: java.lang.Exception) {
            return null
        }

        return null
    }

    private fun getEncryptedHashMap(data: ByteArray): HashMap<String, ByteArray>? {
        try {
            val byteArrayInputStream = ByteArrayInputStream(data)
            val objectInput: ObjectInput
            objectInput = ObjectInputStream(byteArrayInputStream)
            val map = objectInput.readObject()
            objectInput.close()
            byteArrayInputStream.close()

            when (map) {
                is HashMap<*, *> -> {
                    @Suppress("UNCHECKED_CAST")
                    return map as? HashMap<String, ByteArray>
                }
            }
        } catch (ex: java.lang.Exception) {
            return null
        }

        return null
    }

}

private fun <K, V> java.util.HashMap<K, V>.toByteArray(): ByteArray {

    val byteArrayOutputStream = ByteArrayOutputStream()
    val objectOutputStream: ObjectOutputStream

    objectOutputStream = ObjectOutputStream(byteArrayOutputStream)
    objectOutputStream.writeObject(this)
    objectOutputStream.flush()

    val result = byteArrayOutputStream.toByteArray()

    byteArrayOutputStream.close()
    objectOutputStream.close()

    return result
}