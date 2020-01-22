package com.smttcn.commons.crypto

import com.smttcn.commons.helpers.MIN_PASSWORD_LENGTH
import com.smttcn.safebox.MyApplication

class KeyUtil {

    fun isAppDatabaseSecretExist(): Boolean {
        return MyApplication.getBaseConfig().appDatabaseSecretString.length > 20
    }

    fun generateAndSaveAppDatabaseSecret(pwd : String, overwrite: Boolean = false) : String {
        val toCreate = pwd.length >= MIN_PASSWORD_LENGTH && (overwrite || !isAppDatabaseSecretExist())

        if (toCreate) {
            val enc = Encryption()
            val secret = enc.generateSecret()
            val key = enc.encrypt(secret.toByteArray(Charsets.UTF_8), pwd.toCharArray())
            if (key.count() > 0) {
                backupAppDatabaseSecret()
                MyApplication.getBaseConfig().appDatabaseSecretHashMap = key
                return secret
            }
        }
        return ""
    }

    fun reEncryptAndSaveAppDatabaseSecret(oldPwd : String, newPwd : String) {
        // re-encrypt app database secret
        val secret = getAppDatabaseSecretWithAppPassword(oldPwd)
        val enc = Encryption()
        MyApplication.getBaseConfig().appDatabaseSecretHashMap = enc.encrypt(secret.toByteArray(Charsets.UTF_8), newPwd.toCharArray())

    }

    fun getAppDatabaseSecretWithAppPassword(pwd: String) : String {
        if (!isAppDatabaseSecretExist() && pwd.length >= MIN_PASSWORD_LENGTH) {
            generateAndSaveAppDatabaseSecret(pwd, false)
        }
        val secret =  MyApplication.getBaseConfig().appDatabaseSecretHashMap
        val enc = Encryption()
        val dc_pwd = enc.decrypt(secret, pwd.toCharArray())
        return String(dc_pwd!!, Charsets.UTF_8)
    }

    private fun backupAppDatabaseSecret() {
        val bu = MyApplication.getBaseConfig()
        bu.appDatabaseSecretHashMapBackup2 = bu.appDatabaseSecretHashMapBackup1
        bu.appDatabaseSecretHashMapBackup1 = bu.appDatabaseSecretHashMap
    }

}