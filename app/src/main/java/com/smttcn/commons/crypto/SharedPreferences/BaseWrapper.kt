package com.smttcn.commons.crypto.SharedPreferences

/**
 * Designed and Developed by Andrea Cioccarelli
 */
interface BaseWrapper {
    /**
     * Method that returns the encrypted matching string
     * */
    fun encrypt(value: String): String

    /**
     * Method that returns the decrypted matching string
     * */
    fun decrypt(value: String): String
}