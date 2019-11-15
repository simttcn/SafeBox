package com.smttcn.commons.crypto

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import com.smttcn.commons.helpers.KEY_STORE
import com.smttcn.commons.helpers.KEY_STORE_ALIAS
import java.lang.Exception
import java.security.KeyStore
import java.util.*
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import kotlin.collections.ArrayList

internal class KeyStoreUtil {

    private fun getKeyStore() : KeyStore {
        val keyStore = KeyStore.getInstance(KEY_STORE);
        keyStore.load(null)
        return keyStore
    }

    fun isKeyExist(alias: String = KEY_STORE_ALIAS) : Boolean {
        val keyStore = getKeyStore()
        return keyStore.containsAlias(alias)
    }

    fun listKeys() : ArrayList<SecretKey?>{
        val keyStore = getKeyStore()
        val keyAliases : ArrayList<SecretKey?> = ArrayList()
        try {
            val aliases: Enumeration<String> = keyStore.aliases();
            while (aliases.hasMoreElements()) {
                keyAliases.add(getKey(aliases.nextElement()));
            }
        } catch(ex: Exception) {

        }

        return keyAliases
    }

    fun init() {
        val keyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, KEY_STORE)
        val keyGenParameterSpec = KeyGenParameterSpec.Builder(
            KEY_STORE_ALIAS,
            KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT)
            .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
            .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
            //.setUserAuthenticationRequired(true) // 2 requires lock screen, invalidated if lock screen is disabled
            //.setUserAuthenticationValidityDurationSeconds(120) // 3 only available x seconds from password authentication. -1 requires finger print - every time
            .setRandomizedEncryptionRequired(true) // 4 different ciphertext for same plaintext on each call
            .build()
        keyGenerator.init(keyGenParameterSpec)
        keyGenerator.generateKey()

    }

    fun getKey(alias: String = KEY_STORE_ALIAS) : SecretKey? {
        val keyStore = getKeyStore()
        val secretKey: SecretKey

        try {
            val secretKeyEntry = keyStore.getEntry(alias, null) as KeyStore.SecretKeyEntry
            secretKey = secretKeyEntry.secretKey
        } catch (ex: Exception) {
            return null
        }

        return secretKey
    }

    fun deleteKeyByAlias(alias: String) {
        if (alias.isEmpty() || alias.equals(KEY_STORE_ALIAS))
            return

        val keyStore = getKeyStore()
        keyStore.deleteEntry(alias)
    }

}