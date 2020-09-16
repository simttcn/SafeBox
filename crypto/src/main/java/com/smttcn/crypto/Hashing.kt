package com.smttcn.crypto

public class Hashing {

    private val saltLength: Int = 256
    private val keyLength: Int = 256
    private val cpuCost: Int = 16
    private val memoryCost: Int = 16
    private val parallelization: Int = 16

    fun hashWithSalt(originalPassword: String) : String {

        if (originalPassword.isEmpty()) {
            return ""
        }

        val generatedSecuredPasswordHash = SCryptUtil.scrypt(originalPassword, saltLength, keyLength, cpuCost, memoryCost, parallelization)

        return generatedSecuredPasswordHash
    }

    fun hashWithSaltWithVerification(originalPassword: String) : String? {

        val hashedPassword = hashWithSalt(originalPassword)

        if (checkHashWithSalt(originalPassword, hashedPassword))
            return hashedPassword
        else
            return null
    }

    fun checkHashWithSalt(originalPassword: String, hashedPassword: String) : Boolean {

        return SCryptUtil.check(originalPassword, hashedPassword, keyLength)

    }
}