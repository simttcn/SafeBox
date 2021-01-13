package com.smttcn.commons.helpers

import android.content.SharedPreferences
import android.text.format.DateFormat
import androidx.core.content.ContextCompat
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.smttcn.commons.extensions.fromBase64String
import com.smttcn.commons.extensions.lowerCase
import com.smttcn.commons.extensions.toBase64String
import com.smttcn.safebox.MyApplication
import com.smttcn.safebox.R
import java.text.SimpleDateFormat
import java.util.*


open class BaseConfig(val inPrefs: SharedPreferences) {

    val prefs = inPrefs

    companion object {
        fun newInstance(prefs: SharedPreferences) = BaseConfig(prefs)
    }

    var appRunCount: Int
        get() = prefs.getInt(APP_RUN_COUNT, 0)
        set(appRunCount) = prefs.edit().putInt(APP_RUN_COUNT, appRunCount).apply()

    var lastVersion: String
        get() = prefs.getString(LAST_VERSION, "")!!
        set(lastVersion) = prefs.edit().putString(LAST_VERSION, lastVersion).apply()

    var appPasswordEnabled: Boolean
        get() = prefs.getBoolean("enableapppassword", false)
        set(value) = prefs.edit().putBoolean("enableapppassword", value).apply()

    var appPasswordHash: String
        get() = prefs.getString(APP_PASSWORD_HASH_01, "")!!
        set(passwordHash) = prefs.edit().putString(APP_PASSWORD_HASH_01, passwordHash).apply()

    var appPasswordHashBackup: String
        get() = prefs.getString(APP_PASSWORD_HASH_02, "")!!
        set(passwordHash) = prefs.edit().putString(APP_PASSWORD_HASH_02, passwordHash).apply()
    /*
    var appDatabaseSecretHashMap: HashMap<String, ByteArray>
        get() {
            return  HashMap<String, ByteArray>().fromBase64String(prefs.getString(APP_DATABASE_SECRET_ENCRYPTED_01, "")!!)
        }
        set(secret) {
            prefs.edit().putString(APP_DATABASE_SECRET_ENCRYPTED_01, secret.toBase64String()).apply()
        }

    var appDatabaseSecretHashMapBackup1: HashMap<String, ByteArray>
        get() {
            return  HashMap<String, ByteArray>().fromBase64String(prefs.getString(APP_DATABASE_SECRET_ENCRYPTED_02, "")!!)
        }
        set(secret) {
            prefs.edit().putString(APP_DATABASE_SECRET_ENCRYPTED_02, secret.toBase64String()).apply()
        }

    var appDatabaseSecretHashMapBackup2: HashMap<String, ByteArray>
        get() {
            return  HashMap<String, ByteArray>().fromBase64String(prefs.getString(APP_DATABASE_SECRET_ENCRYPTED_03, "")!!)
        }
        set(secret) {
            prefs.edit().putString(APP_DATABASE_SECRET_ENCRYPTED_03, secret.toBase64String()).apply()
        }

    var appUnfinishedReencryptFiles: ArrayList<String>?
        get() {
            val json = prefs.getString(APP_UNFINISHED_REENCRYPT_FILES, null)
            val gson = Gson()
            val type = object : TypeToken<ArrayList<String>>() {}.type
            return gson.fromJson<ArrayList<String>>(json, type)
        }
        set(files) {
            prefs.edit().putString(APP_UNFINISHED_REENCRYPT_FILES, Gson().toJson(files)).apply()
        }

    var appPasswordHashEncrypted: HashMap<String, ByteArray>
        get() {
            val outputMap = HashMap<String, ByteArray>()
            try {
                val jsonString = prefs.getString(APP_PASSWORD_HASH, "")!!
                val jsonObject = JSONObject(jsonString)
                val keysItr = jsonObject.keys()
                while (keysItr.hasNext()) {
                    val key = keysItr.next()
                    val value = jsonObject.getString(key)
                    outputMap[key] = value.toByteArrayEx()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }

            return outputMap
        }
        set(appPasswordHashMap) {
            val inputMap : HashMap<Any?, Any?> = HashMap<Any?, Any?>(appPasswordHashMap)

            appPasswordHashMap.forEach(){
              inputMap[it.key] = Arrays.toString(it.value)
            }

            val jsonObject = JSONObject(inputMap)
            val jsonString = jsonObject.toString()
            prefs.edit().putString(APP_PASSWORD_HASH, jsonString).apply()
        }
    */

    var appId: String
        get() = prefs.getString(APP_ID, "")!!
        set(appId) = prefs.edit().putString(APP_ID, appId).apply()

    var textColor: Int
        get() = prefs.getInt(TEXT_COLOR, ContextCompat.getColor(MyApplication.applicationContext, R.color.colorText))
        set(textColor) = prefs.edit().putInt(TEXT_COLOR, textColor).apply()

    var backgroundColor: Int
        get() = prefs.getInt(BACKGROUND_COLOR, ContextCompat.getColor(MyApplication.applicationContext, R.color.colorBackground))
        set(backgroundColor) = prefs.edit().putInt(BACKGROUND_COLOR, backgroundColor).apply()

    var primaryColor: Int
        get() = prefs.getInt(PRIMARY_COLOR, ContextCompat.getColor(MyApplication.applicationContext, R.color.colorPrimary))
        set(primaryColor) = prefs.edit().putInt(PRIMARY_COLOR, primaryColor).apply()

    fun removePrefKey(key: String) {
        // have to use the same instances for deletion
        // i.e.: assign to an instance and operate on it.
        val editor = prefs.edit()
        editor.remove(key)
        editor.apply()
    }

    private fun getDefaultDateFormat(): String {
        val format = DateFormat.getDateFormat(MyApplication.applicationContext)
        val pattern = (format as SimpleDateFormat).toLocalizedPattern()
        return when (pattern.lowerCase().replace(" ", "")) {
            "dd/mm/y" -> DATE_FORMAT_TWO
            "mm/dd/y" -> DATE_FORMAT_THREE
            "y-mm-dd" -> DATE_FORMAT_FOUR
            else -> DATE_FORMAT_ONE
        }
    }

}
