package com.smttcn.commons.crypto.SharedPreferences


/**
 * Designed and Developed by Andrea Cioccarelli
 */

internal class PrefsWrapper: BaseWrapper {
    override fun encrypt(value: String) = value
    override fun decrypt(value: String) = value
}