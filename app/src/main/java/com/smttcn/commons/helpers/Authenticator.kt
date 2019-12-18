package com.smttcn.commons.helpers

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import com.smttcn.commons.extensions.baseConfig
import com.smttcn.commons.crypto.Encryption
import com.smttcn.commons.crypto.Hashing

internal class Authenticator(appContext: Context) {

    val baseConfig: BaseConfig = BaseConfig.newInstance(appContext)

    public fun isAppPasswordHashExist(): Boolean {
        return baseConfig.appPasswordHash.length > 20
    }

    public fun authenticateAppPassword(password: String, callback: (success: Boolean) -> Unit) {
        val hashed_password = baseConfig.appPasswordHash

        if (hashed_password != null)
        {
            val hasher = Hashing()

            if (hasher.checkHashWithSalt(password, hashed_password)) {
                callback(true)
            } else {
                callback(false)
            }

        } else {
            callback(false)
        }
    }

    public fun newAppPassword(password: String, callback: (success: Boolean) -> Unit) {
        hashAndSavePassword(password, callback)
    }

    public fun changeAppPassword(oldPassword: String, newPassword: String, callback: (success: Boolean) -> Unit) {
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

    public fun hashAndSavePassword(password: String, callback: (success: Boolean) -> Unit) {
        // hash the password
        val hasher = Hashing()
        val hashedPasswordWithSalt = hasher.hashWithSaltWithVerification(password)
        // verify the password before saving the hash
        if (hashedPasswordWithSalt != null)
        {
            baseConfig.appPasswordHash = hashedPasswordWithSalt
            callback(true)
        } else {
            callback(false)
        }

    }


    fun isSimilarByteArray(data1: ByteArray, data2: ByteArray) : Boolean {
        return data1.toString(Charsets.UTF_8).equals(data2.toString(Charsets.UTF_8))
    }

}