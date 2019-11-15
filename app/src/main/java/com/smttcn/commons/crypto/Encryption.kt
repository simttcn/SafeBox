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

package com.smttcn.commons.crypto

import android.annotation.TargetApi
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Log
import com.smttcn.commons.helpers.*
import java.security.KeyStore
import java.security.SecureRandom
import java.util.*
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec

internal class Encryption {

    fun encrypt(dataToEncrypt: ByteArray, password: CharArray): HashMap<String, ByteArray> {
        val map = HashMap<String, ByteArray>()
        try {
            //Random salt for next step
            val random = SecureRandom()
            val salt = ByteArray(KEY_SALT_LENGTH)
            random.nextBytes(salt)

            //PBKDF2 - derive the key from the password, don't use passwords directly
            val pbKeySpec = PBEKeySpec(
                password, salt,
                KEY_HASH_ITERATION_COUNT,
                KEY_LENGTH
            )
            val secretKeyFactory = SecretKeyFactory.getInstance(KEY_FACTORY_ALGORITHM)
            val keyBytes = secretKeyFactory.generateSecret(pbKeySpec).encoded
            val keySpec = SecretKeySpec(keyBytes, KEY_SPEC_ALGORITHM)

            //Create initialization vector for AES
            val ivRandom = SecureRandom() //not caching previous seeded instance of SecureRandom
            val iv = ByteArray(KEY_IV_LENGTH)
            ivRandom.nextBytes(iv)
            val ivSpec = IvParameterSpec(iv)

            //Encrypt
            val cipher = Cipher.getInstance(KEY_PBKDF2_TRANSFORMATION)
            cipher.init(Cipher.ENCRYPT_MODE, keySpec, ivSpec)
            val encrypted = cipher.doFinal(dataToEncrypt)

            map[KEY_TEXT_SALT] = salt
            map[KEY_TEXT_IV] = iv
            map[KEY_TEXT_ENCRYPTED] = encrypted
        } catch (e: Exception) {
            Log.e("MYAPP", "encryption exception", e)
        }

        return map

    }

    fun decrypt(map: HashMap<String, ByteArray>, password: CharArray): ByteArray? {
        var decrypted: ByteArray? = null
        try {
            val salt = map[KEY_TEXT_SALT]
            val iv = map[KEY_TEXT_IV]
            val encrypted = map[KEY_TEXT_ENCRYPTED]

            //regenerate key from password
            val pbKeySpec = PBEKeySpec(
                password, salt,
                KEY_HASH_ITERATION_COUNT,
                KEY_LENGTH
            )
            val secretKeyFactory = SecretKeyFactory.getInstance(KEY_FACTORY_ALGORITHM)
            val keyBytes = secretKeyFactory.generateSecret(pbKeySpec).encoded
            val keySpec = SecretKeySpec(keyBytes, KEY_SPEC_ALGORITHM)

            //Decrypt
            val cipher = Cipher.getInstance(KEY_PBKDF2_TRANSFORMATION)
            val ivSpec = IvParameterSpec(iv)
            cipher.init(Cipher.DECRYPT_MODE, keySpec, ivSpec)
            decrypted = cipher.doFinal(encrypted)
        } catch (e: Exception) {
            Log.e("MYAPP", "decryption exception", e)
        }

        return decrypted
    }

    fun keystoreEncryptWithVerification(dataToEncrypt: String, callback : (result: Boolean, encryptedData : HashMap<String, ByteArray>) -> Unit) {

        val encryptedData = keystoreEncrypt(dataToEncrypt)
        val decryptedData = keystoreDecrypt(encryptedData)

        if (dataToEncrypt.equals(decryptedData ))
            return callback(true, encryptedData)
        else
            return callback(false, encryptedData)
    }

    fun keystoreEncrypt(dataToEncrypt: String): HashMap<String, ByteArray> {
        val map = HashMap<String, ByteArray>()
        try {
            //Get the key
            val keyStoreUtil = KeyStoreUtil()
            val secretKey = keyStoreUtil.getKey()

            //Encrypt data
            val cipher = Cipher.getInstance(KEY_CRYPTOR_TRANSFORMATION)
            cipher.init(Cipher.ENCRYPT_MODE, secretKey)
            val ivBytes = cipher.iv
            val encryptedBytes = cipher.doFinal(dataToEncrypt.toByteArray(Charsets.UTF_8))

            map[KEY_TEXT_IV] = ivBytes
            map[KEY_TEXT_ENCRYPTED] = encryptedBytes
        } catch (e: Throwable) {
            e.printStackTrace()
        }

        return map
    }

    fun keystoreDecrypt(map: HashMap<String, ByteArray>): String {
        var decrypted: String = ""
        try {
            //Get the key
            val keyStoreUtil = KeyStoreUtil()
            val secretKey = keyStoreUtil.getKey()

            //Extract info from map
            val encryptedBytes = map[KEY_TEXT_ENCRYPTED]
            val ivBytes = map[KEY_TEXT_IV]

            //Decrypt data
            val cipher = Cipher.getInstance(KEY_CRYPTOR_TRANSFORMATION)
            val spec = GCMParameterSpec(128, ivBytes)
            cipher.init(Cipher.DECRYPT_MODE, secretKey, spec)
            decrypted = cipher.doFinal(encryptedBytes).toString(Charsets.UTF_8)

        } catch (e: Throwable) {
            e.printStackTrace()
        }

        return decrypted
    }

    @TargetApi(23)
    fun keystoreTest() {

        val keyGenerator =
            KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore")
        val keyGenParameterSpec = KeyGenParameterSpec.Builder(
            KEY_STORE_ALIAS,
            KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
            )
            .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
            .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
            //.setUserAuthenticationRequired(true) // 2 requires lock screen, invalidated if lock screen is disabled
            //.setUserAuthenticationValidityDurationSeconds(120) // 3 only available x seconds from password authentication. -1 requires finger print - every time
            .setRandomizedEncryptionRequired(true) // 4 different ciphertext for same plaintext on each call
            .build()
        keyGenerator.init(keyGenParameterSpec)
        keyGenerator.generateKey()

        val map = keystoreEncrypt("My very sensitive string!")
        val decryptedBytes = keystoreDecrypt(map)
        decryptedBytes.let {
            val decryptedString = it
            Log.e("MyApp", "The decrypted string is: $decryptedString")
        }
    }
}

