package com.smttcn.commons.crypto

import java.security.SecureRandom
import java.util.HashMap
import com.smttcn.commons.helpers.KEY_SALT_LENGTH
import timber.log.Timber
import android.R.attr.password
import com.smttcn.commons.helpers.KEY_LENGTH


internal class Hashing {

    private val saltLength: Int = KEY_SALT_LENGTH
    private val keyLength: Int = KEY_LENGTH
    private val cpuCost: Int = 16
    private val memoryCost: Int = 16
    private val parallelization: Int = 16

    fun HashWithSalt(originalPassword: String) : String {

        if (originalPassword.isEmpty()) {
            return ""
        }

        val generatedSecuredPasswordHash = SCryptUtil.scrypt(originalPassword, saltLength, keyLength,cpuCost, memoryCost, parallelization)

        return generatedSecuredPasswordHash
    }

    fun CheckHashWithSalt(originalPassword: String, hashedPassword: String) : Boolean {

        return SCryptUtil.check(originalPassword, hashedPassword, keyLength)

    }
}