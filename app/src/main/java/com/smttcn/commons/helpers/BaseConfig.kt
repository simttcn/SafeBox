package com.smttcn.commons.helpers

import android.content.SharedPreferences
import android.text.format.DateFormat
import com.smttcn.safebox.R
import java.text.SimpleDateFormat
import java.util.*
import androidx.core.content.ContextCompat
import com.smttcn.commons.extensions.fromBase64String
import com.smttcn.commons.extensions.toBase64String
import com.smttcn.safebox.MyApplication


open class BaseConfig(val inPrefs: SharedPreferences) {

    val prefs = inPrefs
    val context = MyApplication.getAppContext()

    companion object {
        fun newInstance(prefs: SharedPreferences) = BaseConfig(prefs)
    }

    var appRunCount: Int
        get() = prefs.getInt(APP_RUN_COUNT, 0)
        set(appRunCount) = prefs.edit().putInt(APP_RUN_COUNT, appRunCount).apply()

    var lastVersion: Int
        get() = prefs.getInt(LAST_VERSION, 0)
        set(lastVersion) = prefs.edit().putInt(LAST_VERSION, lastVersion).apply()

    var appPasswordHash: String
        get() = prefs.getString(APP_PASSWORD_HASH, "")!!
        set(passwordHash) = prefs.edit().putString(APP_PASSWORD_HASH, passwordHash).apply()

    var appDatabaseSecretString: String
        get() = prefs.getString(APP_DATABASE_SECRET_ENCRYPTED_01, "")!!
        set(secret) = prefs.edit().putString(APP_DATABASE_SECRET_ENCRYPTED_01, secret).apply()

    var appDatabaseSecretStringBackup1: String
        get() = prefs.getString(APP_DATABASE_SECRET_ENCRYPTED_02, "")!!
        set(secret) = prefs.edit().putString(APP_DATABASE_SECRET_ENCRYPTED_02, secret).apply()

    var appDatabaseSecretStringBackup2: String
        get() = prefs.getString(APP_DATABASE_SECRET_ENCRYPTED_03, "")!!
        set(secret) = prefs.edit().putString(APP_DATABASE_SECRET_ENCRYPTED_03, secret).apply()

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

//    var appPasswordHashEncrypted: HashMap<String, ByteArray>
//        get() {
//            val outputMap = HashMap<String, ByteArray>()
//            try {
//                val jsonString = prefs.getString(APP_PASSWORD_HASH, "")!!
//                val jsonObject = JSONObject(jsonString)
//                val keysItr = jsonObject.keys()
//                while (keysItr.hasNext()) {
//                    val key = keysItr.next()
//                    val value = jsonObject.getString(key)
//                    outputMap[key] = value.toByteArrayEx()
//                }
//            } catch (e: Exception) {
//                e.printStackTrace()
//            }
//
//            return outputMap
//        }
//        set(appPasswordHashMap) {
//            val inputMap : HashMap<Any?, Any?> = HashMap<Any?, Any?>(appPasswordHashMap)
//
//            appPasswordHashMap.forEach(){
//              inputMap[it.key] = Arrays.toString(it.value)
//            }
//
//            val jsonObject = JSONObject(inputMap)
//            val jsonString = jsonObject.toString()
//            prefs.edit().putString(APP_PASSWORD_HASH, jsonString).apply()
//        }

    var sorting: Int
        get() = prefs.getInt(SORT_ORDER, context.resources.getInteger(R.integer.default_sorting))
        set(sorting) = prefs.edit().putInt(SORT_ORDER, sorting).apply()

    var use24HourFormat: Boolean
        get() = prefs.getBoolean(USE_24_HOUR_FORMAT, DateFormat.is24HourFormat(context))
        set(use24HourFormat) = prefs.edit().putBoolean(USE_24_HOUR_FORMAT, use24HourFormat).apply()

    var isSundayFirst: Boolean
        get() {
            val isSundayFirst = Calendar.getInstance(Locale.getDefault()).firstDayOfWeek == Calendar.SUNDAY
            return prefs.getBoolean(SUNDAY_FIRST, isSundayFirst)
        }
        set(sundayFirst) = prefs.edit().putBoolean(SUNDAY_FIRST, sundayFirst).apply()

    var appId: String
        get() = prefs.getString(APP_ID, "")!!
        set(appId) = prefs.edit().putString(APP_ID, appId).apply()

    var dateFormat: String
        get() = prefs.getString(DATE_FORMAT, getDefaultDateFormat())!!
        set(dateFormat) = prefs.edit().putString(DATE_FORMAT, dateFormat).apply()

    var textColor: Int
        get() = prefs.getInt(TEXT_COLOR, ContextCompat.getColor(context, R.color.colorText))
        set(textColor) = prefs.edit().putInt(TEXT_COLOR, textColor).apply()

    var backgroundColor: Int
        get() = prefs.getInt(BACKGROUND_COLOR, ContextCompat.getColor(context, R.color.colorBackground))
        set(backgroundColor) = prefs.edit().putInt(BACKGROUND_COLOR, backgroundColor).apply()

    var primaryColor: Int
        get() = prefs.getInt(PRIMARY_COLOR, ContextCompat.getColor(context, R.color.colorPrimary))
        set(primaryColor) = prefs.edit().putInt(PRIMARY_COLOR, primaryColor).apply()

    private fun getDefaultDateFormat(): String {
        val format = DateFormat.getDateFormat(context)
        val pattern = (format as SimpleDateFormat).toLocalizedPattern()
        return when (pattern.toLowerCase().replace(" ", "")) {
            "dd/mm/y" -> DATE_FORMAT_TWO
            "mm/dd/y" -> DATE_FORMAT_THREE
            "y-mm-dd" -> DATE_FORMAT_FOUR
            else -> DATE_FORMAT_ONE
        }
    }

}
