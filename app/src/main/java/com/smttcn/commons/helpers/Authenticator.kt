package com.smttcn.commons.helpers

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import com.smttcn.commons.crypto.Encryption
import com.smttcn.commons.crypto.Hashing
import com.smttcn.safebox.MyApplication

internal class Authenticator() {

    public fun isAppPasswordHashExist(): Boolean {
        return MyApplication.getBaseConfig().appPasswordHash.length > 20
    }

    public fun isAppDatabaseSecretExist(): Boolean {
        return MyApplication.getBaseConfig().appDatabaseSecretString.length > 20
    }

    public fun generateAndSaveAppDatabaseSecret(pwd : String, overwrite: Boolean = false) : String {
        val toCreate = pwd.length >= MIN_PASSWORD_LENGTH && (overwrite || !isAppDatabaseSecretExist())

        if (toCreate) {
            val enc = Encryption()
            val secret = enc.generateSecret()
            MyApplication.getBaseConfig().appDatabaseSecretHashMap = enc.encrypt(secret.toByteArray(Charsets.UTF_8), pwd.toCharArray())
            return secret
        }
        return ""
    }

    public fun reEncryptAndSaveAppDatabaseSecret(oldPwd : String, newPwd : String) {
        // Todo: re encrypt app database secret
        val secret = getAppDatabaseSecretWithAppPassword(oldPwd)
        val enc = Encryption()
        MyApplication.getBaseConfig().appDatabaseSecretHashMap = enc.encrypt(secret.toByteArray(Charsets.UTF_8), newPwd.toCharArray())

    }

    public fun getAppDatabaseSecretWithAppPassword(pwd: String) : String {
        if (!isAppDatabaseSecretExist() && pwd.length >= MIN_PASSWORD_LENGTH) {
            generateAndSaveAppDatabaseSecret(pwd, false)
        }
        val secret =  MyApplication.getBaseConfig().appDatabaseSecretHashMap
        val enc = Encryption()
        val dc_pwd = enc.decrypt(secret, pwd.toCharArray())
        return String(dc_pwd!!, Charsets.UTF_8)
    }

    public fun authenticateAppPassword(password: String, callback: (success: Boolean) -> Unit) {
        val hashed_password = MyApplication.getBaseConfig().appPasswordHash

        val hasher = Hashing()

        if (hasher.checkHashWithSalt(password, hashed_password)) {
            callback(true)
        } else {
            callback(false)
        }
    }

    public fun newAppPassword(password: String, callback: (success: Boolean) -> Unit) {
        hashAndSavePassword("", password, callback)
    }

    public fun changeAppPassword(oldPassword: String, newPassword: String, callback: (success: Boolean) -> Unit) {
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

    public fun hashAndSavePassword(oldPassword : String, newPassword: String, callback: (success: Boolean) -> Unit) {
        // hash the password
        val hasher = Hashing()
        // verify the password before saving the hash
        val hashedPasswordWithSalt = hasher.hashWithSaltWithVerification(newPassword)
        if (hashedPasswordWithSalt != null)
        {
            MyApplication.getBaseConfig().appPasswordHash = hashedPasswordWithSalt
            if (oldPassword.length >= MIN_PASSWORD_LENGTH) {
                reEncryptAndSaveAppDatabaseSecret(oldPassword, newPassword)
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