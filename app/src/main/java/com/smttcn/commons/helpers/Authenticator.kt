package com.smttcn.commons.helpers

import com.smttcn.commons.crypto.Hashing
import com.smttcn.safebox.MyApplication

internal class Authenticator() {

    fun isAppPasswordHashExist(): Boolean {
        return MyApplication.baseConfig.appPasswordHash.length > 20
    }


    fun authenticateAppPassword(password: String, callback: (success: Boolean) -> Unit) {
        val hashed_password = MyApplication.baseConfig.appPasswordHash

        val hasher = Hashing()

        if (hasher.checkHashWithSalt(password, hashed_password)) {
            callback(true)
        } else {
            callback(false)
        }
    }

    fun newAppPassword(password: String, callback: (success: Boolean) -> Unit) {
        hashAndSavePassword(password, callback)
    }

    fun removeAppPassword(currentPassword: String, callback: (success: Boolean) -> Unit) {
        authenticateAppPassword(currentPassword) {
            if (it == true) {
                MyApplication.baseConfig.appPasswordHash = ""
                MyApplication.baseConfig.appPasswordHashBackup = ""
                MyApplication.baseConfig.removePrefKey(APP_PASSWORD_HASH_01)
                MyApplication.baseConfig.removePrefKey(APP_PASSWORD_HASH_02)
                callback(true)
            } else {
                callback(false)
            }
        }
    }

    fun changeAppPassword(oldPassword: String, newPassword: String, callback: (success: Boolean) -> Unit) {
        // check the current password again before saving the new hashed oassword
        authenticateAppPassword(oldPassword) {
            if (it == true) {
                // current password matched, so go ahead to hash and save the new password
                hashAndSavePassword(newPassword, callback)
            } else {
                // current password not matched
                callback(false)
            }
        }
    }

    fun hashAndSavePassword(newPassword: String, callback: (success: Boolean) -> Unit) {
        // hash the password
        val hasher = Hashing()
        // verify the password before saving the hash
        val hashedPasswordWithSalt = hasher.hashWithSaltWithVerification(newPassword)
        if (hashedPasswordWithSalt != null)
        {
            MyApplication.baseConfig.appPasswordHashBackup = MyApplication.baseConfig.appPasswordHash
            MyApplication.baseConfig.appPasswordHash = hashedPasswordWithSalt
            callback(true)
        } else {
            callback(false)
        }

    }


    fun isSimilarByteArray(data1: ByteArray, data2: ByteArray) : Boolean {
        return data1.toString(Charsets.UTF_8).equals(data2.toString(Charsets.UTF_8))
    }

}