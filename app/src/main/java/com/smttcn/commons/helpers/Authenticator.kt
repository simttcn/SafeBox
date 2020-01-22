package com.smttcn.commons.helpers

import com.smttcn.commons.crypto.Hashing
import com.smttcn.commons.crypto.KeyUtil
import com.smttcn.safebox.MyApplication

internal class Authenticator() {

    fun isAppPasswordHashExist(): Boolean {
        return MyApplication.getBaseConfig().appPasswordHash.length > 20
    }


    fun authenticateAppPassword(password: String, callback: (success: Boolean) -> Unit) {
        val hashed_password = MyApplication.getBaseConfig().appPasswordHash

        val hasher = Hashing()

        if (hasher.checkHashWithSalt(password, hashed_password)) {
            callback(true)
        } else {
            callback(false)
        }
    }

    fun newAppPassword(password: String, callback: (success: Boolean) -> Unit) {
        hashAndSavePassword("", password, callback)
    }

    fun changeAppPassword(oldPassword: String, newPassword: String, callback: (success: Boolean) -> Unit) {
        // check the current password again before saving the new hashed oassword
        authenticateAppPassword(oldPassword) {
            if (it == true) {
                // current password matched, so go ahead to hash and save the new password
                hashAndSavePassword(oldPassword, newPassword, callback)
            } else {
                // current password not matched
                callback(false)
            }
        }
    }

    fun hashAndSavePassword(oldPassword : String, newPassword: String, callback: (success: Boolean) -> Unit) {
        // hash the password
        val hasher = Hashing()
        // verify the password before saving the hash
        val hashedPasswordWithSalt = hasher.hashWithSaltWithVerification(newPassword)
        if (hashedPasswordWithSalt != null)
        {
            MyApplication.getBaseConfig().appPasswordHash = hashedPasswordWithSalt
            if (oldPassword.length >= MIN_PASSWORD_LENGTH) {
                val keyUtil = KeyUtil()
                keyUtil.reEncryptAndSaveAppDatabaseSecret(oldPassword, newPassword)
            }
            callback(true)
        } else {
            callback(false)
        }

    }


    fun isSimilarByteArray(data1: ByteArray, data2: ByteArray) : Boolean {
        return data1.toString(Charsets.UTF_8).equals(data2.toString(Charsets.UTF_8))
    }

}