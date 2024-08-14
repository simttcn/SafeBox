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

import android.telephony.mbms.MbmsErrors
import java.io.*
import java.nio.ByteBuffer
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.CipherInputStream
import javax.crypto.CipherOutputStream
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec
import kotlin.ByteArray

// Todo: reading and writing new fields PROP in file header
public class Encryption constructor(ver: Int) {
//Todo: check for support file version
    val VER_LENGTH_BIT : Int = 32 // 4 bytes
    val IV_LENGTH_BIT : Int = 128 // 16 bytes
    val SALT_LENGTH_BIT : Int = 256 // 32 bytes
    val PROP_LENGTH_BIT : Int = 4096 // 512 bytes

    val VER_LENGTH_BYTE : Int = VER_LENGTH_BIT / 8
    val IV_LENGTH_BYTE : Int = IV_LENGTH_BIT / 8
    val SALT_LENGTH_BYTE : Int = SALT_LENGTH_BIT / 8
    val PROP_LENGTH_BYTE : Int = PROP_LENGTH_BIT / 8

    val KEY_LENGTH_BIT : Int = 256 // 32 bytes

    val VER_NO = ver
    val SECRETKEYFACTORY_ALGORITHM = "PBKDF2WithHmacSHA1"
    val CIPHER_TRANSFORMATION = "AES/CBC/PKCS7Padding"
    val KEY_HASH_ITERATION_COUNT = 10000
    //val CIPHER_TRANSFORMATION = "AES/GCM/NoPadding" // slower than CBC for local file encryption

    fun generateRandomByte(length : Int = 16) : ByteArray {
        val random = SecureRandom()
        val data = ByteArray(length)
        random.nextBytes(data)
        return data
    }

    fun getVerByteArray() : ByteArray {
        val bb = ByteBuffer.allocate(VER_LENGTH_BYTE)
        bb.clear()
        bb.putInt(VER_NO)
        return bb.array()
    }

    fun getVerFromByteArray(bytes: ByteArray): Int {
        var result = 0
        for (i in bytes.indices) {
            result = result or (bytes[i].toInt() shl 32 * i)
        }
        return result
    }

    fun generateSecretKey(length : Int = 16) : ByteArray {
        return generateRandomByte(length)
    }

    fun generateSecretKeyAsString(length : Int = 16) : String {
        return generateRandomByte(length).toString()
    }

    fun generateSecretKeyAsBase64CharArray(length : Int = 16) : CharArray {
        return String(Base64.encode(generateRandomByte(length))).dropLast(2).toCharArray()
    }

    // encrypt byte array to byte array
    fun encryptByteArrayToTripleByteArray(data : ByteArray, password : CharArray) : Triple<ByteArray, ByteArray, ByteArray>?{

        val salt = this.generateRandomByte(SALT_LENGTH_BYTE)
        val cipher = getCipher(Cipher.ENCRYPT_MODE, password, salt, null)

        return Triple(cipher.iv, salt, cipher.doFinal(data))
    }

    // encrypt input stream to file
    fun encryptInputStreamToFile(input : InputStream, targetFilepath : String, password : CharArray, overwite : Boolean = false) : Boolean{

        val salt = this.generateRandomByte(SALT_LENGTH_BYTE)
        val cipher = getCipher(Cipher.ENCRYPT_MODE, password, salt, null)
        // Todo: set prop here
        val prop = ByteArray(PROP_LENGTH_BYTE)

        // is the input okay
        if (input.available() < 1)
            return false

        val outFile = File(targetFilepath)

        // if not overwrite and target file already exist
        if (!overwite && outFile.exists())
            return false

        outFile.createNewFile()

        try {
            val fileOutputStream = FileOutputStream(outFile, true)
            val cOut = CipherOutputStream(fileOutputStream, cipher)

            fileOutputStream.write(getVerByteArray(), 0, getVerByteArray().size)
            fileOutputStream.write(cipher.iv, 0, cipher.iv.size)
            fileOutputStream.write(salt, 0, salt.size)
            fileOutputStream.write(prop, 0, prop.size)

            var buffer = ByteArray(DEFAULT_BUFFER_SIZE)
            var count = input.read(buffer)

            while (count> 0) {
                cOut.write(buffer, 0, count)
                count = input.read(buffer)
            }
            cOut.flush()
            cOut.close()
            fileOutputStream.close()
            input.close()

            return true

        } catch (e: Exception) {

        }

        return false
    }

    // encrypt file to file
    fun encryptFileToFile(sourceFilepath : String, targetFilepath : String, password : CharArray, overwite : Boolean = false) : Boolean{

        val salt = this.generateRandomByte(SALT_LENGTH_BYTE)
        val cipher = getCipher(Cipher.ENCRYPT_MODE, password, salt, null)
        // Todo: set prop here
        val prop = ByteArray(PROP_LENGTH_BYTE)

        val inFile = File(sourceFilepath)
        val outFile = File(targetFilepath)

        // is source file exist
        if (!inFile.exists())
            return false

        // if not overwrite and target file already exist
        if (!overwite && outFile.exists())
            return false

        if (outFile.exists() && overwite)
            outFile.delete()
        else
            return false

        outFile.createNewFile()

        try {
            val fileInputStream = FileInputStream(inFile)
            val fileOutputStream = FileOutputStream(outFile, true)
            val cOut = CipherOutputStream(fileOutputStream, cipher)

            fileOutputStream.write(getVerByteArray(), 0, getVerByteArray().size)
            fileOutputStream.write(cipher.iv, 0, cipher.iv.size)
            fileOutputStream.write(salt, 0, salt.size)
            fileOutputStream.write(prop, 0, prop.size)

            var buffer = ByteArray(DEFAULT_BUFFER_SIZE)
            var count = fileInputStream.read(buffer)

            while (count> 0) {
                cOut.write(buffer, 0, count)
                count = fileInputStream.read(buffer)
            }
            cOut.flush()
            cOut.close()
            fileOutputStream.close()
            fileInputStream.close()

            return true

        } catch (e: Exception) {

        }

        return false
    }


    // decrypt input stream to file
    fun decryptInputStreamToFile(input : InputStream, targetFilepath : String, password : CharArray, overwite : Boolean = false) : Boolean{

        // is the input okay
        if (input.available() < 1)
            return false

        val outFile = File(targetFilepath)

        if (outFile.exists()) {
            if (overwite)
                outFile.delete()
            else
                return false
        }

        outFile.createNewFile()

        try {
            var ver : ByteArray = ByteArray(VER_LENGTH_BYTE)
            var iv : ByteArray = ByteArray(IV_LENGTH_BYTE)
            var salt : ByteArray = ByteArray(SALT_LENGTH_BYTE)
            var prop : ByteArray = ByteArray(PROP_LENGTH_BYTE)

            input.read(ver, 0, VER_LENGTH_BYTE)
            input.read(iv, 0, IV_LENGTH_BYTE)
            input.read(salt, 0, SALT_LENGTH_BYTE)
            input.read(prop, 0, PROP_LENGTH_BYTE)

            //val ver_no : Int = getVerFromByteArray(ver)

            if (iv.size > 0 && salt.size > 0) {

                val cipher = getCipher(Cipher.DECRYPT_MODE, password, salt, iv)
                val fileOutputStream = FileOutputStream(outFile, false)
                val cOut = CipherOutputStream(fileOutputStream, cipher)

                var buffer = ByteArray(DEFAULT_BUFFER_SIZE)
                var count = input.read(buffer)

                while (count> 0) {
                    cOut.write(buffer, 0, count)
                    count = input.read(buffer)
                }
                cOut.flush()
                cOut.close()
                fileOutputStream.close()
                input.close()

                return true

            }

        } catch (e: Exception) {

        }

        return false
    }

    // decrypt file to file
    fun decryptFileToFile(sourceFilepath : String, targetFilepath : String, password : CharArray, overwite : Boolean = false) : Boolean{

        val inFile = File(sourceFilepath)
        val outFile = File(targetFilepath)

        // is source file exist
        if (!inFile.exists())
            return false

        // if not overwrite and target file already exist
        if (!overwite && outFile.exists())
            return false

        outFile.createNewFile()

        try {
            var ver : ByteArray = ByteArray(VER_LENGTH_BYTE)
            var iv : ByteArray = ByteArray(IV_LENGTH_BYTE)
            var salt : ByteArray = ByteArray(SALT_LENGTH_BYTE)
            var prop : ByteArray = ByteArray(PROP_LENGTH_BYTE)
            val fileInputStream = FileInputStream(inFile)

            fileInputStream.read(ver, 0, VER_LENGTH_BYTE)
            fileInputStream.read(iv, 0, IV_LENGTH_BYTE)
            fileInputStream.read(salt, 0, SALT_LENGTH_BYTE)
            fileInputStream.read(prop, 0, PROP_LENGTH_BYTE)

            //val ver_no : Int = getVerFromByteArray(ver)

            if (iv.size > 0 && salt.size > 0) {

                val cipher = getCipher(Cipher.DECRYPT_MODE, password, salt, iv)
                val fileOutputStream = FileOutputStream(outFile, false)
                val cOut = CipherOutputStream(fileOutputStream, cipher)

                var buffer = ByteArray(DEFAULT_BUFFER_SIZE)
                var count = fileInputStream.read(buffer)

                while (count> 0) {
                    cOut.write(buffer, 0, count)
                    count = fileInputStream.read(buffer)
                }
                cOut.flush()
                cOut.close()
                fileOutputStream.close()
                fileInputStream.close()

                return true

            }

        } catch (e: Exception) {

        }

        return false
    }

    // decrypt file to byte array
    fun decryptFileToByteArray(sourceFilepath : String, password : CharArray) : ByteArray?{

        val inFile = File(sourceFilepath)

        // is source file exist
        if (!inFile.exists())
            return null

        try {
            var ver : ByteArray = ByteArray(VER_LENGTH_BYTE)
            var iv : ByteArray = ByteArray(IV_LENGTH_BYTE)
            var salt : ByteArray = ByteArray(SALT_LENGTH_BYTE)
            var prop : ByteArray = ByteArray(PROP_LENGTH_BYTE)
            val fileInputStream = FileInputStream(inFile)

            fileInputStream.read(ver, 0, VER_LENGTH_BYTE)
            fileInputStream.read(iv, 0, IV_LENGTH_BYTE)
            fileInputStream.read(salt, 0, SALT_LENGTH_BYTE)
            fileInputStream.read(prop, 0, PROP_LENGTH_BYTE)

            //val ver_no : Int = getVerFromByteArray(ver)

            if (iv.size > 0 && salt.size > 0) {

                val cipher = getCipher(Cipher.DECRYPT_MODE, password, salt, iv)
                val cIn = CipherInputStream(fileInputStream, cipher)

                val result= cIn.readBytes()

                cIn.close()
                fileInputStream.close()

                return result

            }

        } catch (e: Exception) {

        }

        return null
    }


    private fun getCipher(mode: Int, password: CharArray, s: ByteArray, i: ByteArray?) : Cipher {

        var salt = s
        var iv = i

        if (mode == Cipher.ENCRYPT_MODE) {
            // create a random iv if in encrypt mode
            iv = this.generateRandomByte(IV_LENGTH_BYTE)
        }

        val pbKeySpec = PBEKeySpec(password, salt, KEY_HASH_ITERATION_COUNT, KEY_LENGTH_BIT)
        val secretKeyFactory = SecretKeyFactory.getInstance(SECRETKEYFACTORY_ALGORITHM)
        val keyBytes = secretKeyFactory.generateSecret(pbKeySpec).encoded
        val keySpec = SecretKeySpec(keyBytes, "AES")

        val ivSpec = IvParameterSpec(iv)

        val cipher = Cipher.getInstance(CIPHER_TRANSFORMATION)
        cipher.init(mode, keySpec, ivSpec)

        return cipher
    }

}