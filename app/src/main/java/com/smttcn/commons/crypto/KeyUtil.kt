package com.smttcn.commons.crypto

import com.smttcn.commons.helpers.APP_ENCRYPT_VAR2
import com.smttcn.commons.helpers.APP_ENCRYPT_VAR1
import com.smttcn.commons.helpers.APP_ENCRYPT_VAR3
import com.smttcn.commons.helpers.MIN_PASSWORD_LENGTH
import com.smttcn.safebox.MyApplication

class KeyUtil {

    fun isAppDatabaseSecretExist(): Boolean {
        return MyApplication.getBaseConfig().appDatabaseSecretHashMap.containsKey(APP_ENCRYPT_VAR2)
                && MyApplication.getBaseConfig().appDatabaseSecretHashMap.containsKey(
            APP_ENCRYPT_VAR1
        )
                && MyApplication.getBaseConfig().appDatabaseSecretHashMap.containsKey(
            APP_ENCRYPT_VAR3
        )
    }

    fun generateAndSaveAppDatabaseSecret(pwd : CharArray, overwrite: Boolean = false) : String {
        val toCreate = pwd.size >= MIN_PASSWORD_LENGTH && (overwrite || !isAppDatabaseSecretExist())

        if (toCreate) {
            val enc = Encryption()
            val secret = enc.generateSecret()
            encryptDatabaseSecretWithBackup(pwd, secret)
            return secret
        }
        return ""
    }

    fun reEncryptAndSaveAppDatabaseSecret(oldPwd: CharArray, newPwd: CharArray) {
        // re-encrypt app database secret
        val secret = getAppDatabaseSecretWithAppPassword(oldPwd)
        encryptDatabaseSecretWithBackup(newPwd, secret)
    }

    fun getAppDatabaseSecretWithAppPassword(pwd: CharArray) : String {
        if (!isAppDatabaseSecretExist() && pwd.size >= MIN_PASSWORD_LENGTH) {
            generateAndSaveAppDatabaseSecret(pwd, false)
        }
        val secret =  MyApplication.getBaseConfig().appDatabaseSecretHashMap
        val enc = Encryption()
        val dc_pwd = enc.decrypt(secret, pwd)
        return String(dc_pwd!!, Charsets.UTF_8)
    }

    private fun encryptDatabaseSecretWithBackup(pwd: CharArray, secret: String) {
        val enc = Encryption()
        val key = enc.encrypt(secret.toByteArray(Charsets.UTF_8), pwd)
        if (key.containsKey(APP_ENCRYPT_VAR2) && key.containsKey(APP_ENCRYPT_VAR1) && key.containsKey(APP_ENCRYPT_VAR3)) {
            backupAppDatabaseSecret()
            MyApplication.getBaseConfig().appDatabaseSecretHashMap = key
        }
    }

    private fun backupAppDatabaseSecret() {
        val bc = MyApplication.getBaseConfig()
        bc.appDatabaseSecretHashMapBackup2 = bc.appDatabaseSecretHashMapBackup1
        bc.appDatabaseSecretHashMapBackup1 = bc.appDatabaseSecretHashMap
    }

}