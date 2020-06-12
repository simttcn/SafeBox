package com.smttcn.commons.helpers

import android.os.Build
import android.os.Looper

const val TEMP_PASSWORD = "123456" //todo: temp password from testing

const val APP_NAME = "app_name"
const val APP_LICENSES = "app_licenses"
const val APP_FAQ = "app_faq"
const val APP_VERSION_NAME = "app_version_name"
const val APP_ICON_IDS = "app_icon_ids"
const val APP_ID = "app_id"
const val APP_LAUNCHER_NAME = "app_launcher_name"
const val APP_ENCRYPT_VAR0 = "var0" //filename
const val APP_ENCRYPT_VAR1 = "var1" //iv
const val APP_ENCRYPT_VAR2 = "var2" //salt
const val APP_ENCRYPT_VAR3 = "var3" //encrypted
const val APP_PASSWORD_HASH_01 = "key_00"
const val APP_PASSWORD_HASH_02 = "key_01"
const val KEY_HASH_ITERATION_COUNT = 10000
const val MIN_PASSWORD_LENGTH = 6

const val INTERVAL_BACK_BUTTON_QUIT_IN_MS = 3000

const val SIZE_THUMBNAIL_WIDTH = 48
const val SIZE_THUMBNAIL_HEIGHT = 48

// activity for result request code
const val REQUEST_CODE_NEW_APP_PASSWORD = 100
const val REQUEST_CODE_CHANGE_APP_PASSWORD = 110

// Intent request string
const val INTENT_CALL_FROM_MAINACTIVITY = "call_from_mainactivity"
const val INTENT_TO_CREATE_APP_PASSWORD = "to_create_app_password"

// date and time
const val USE_24_HOUR_FORMAT = "use_24_hour_format"
const val SUNDAY_FIRST = "sunday_first"
const val DATE_FORMAT = "date_format"
const val CHOPPED_LIST_DEFAULT_SIZE = 50
const val NOMEDIA = ".nomedia"

const val TEXT_COLOR = "text_color"
const val BACKGROUND_COLOR = "background_color"
const val PRIMARY_COLOR = "primary_color_2"

const val HOUR_MINUTES = 60
const val DAY_MINUTES = 24 * HOUR_MINUTES
const val WEEK_MINUTES = DAY_MINUTES * 7
const val MONTH_MINUTES = DAY_MINUTES * 30
const val YEAR_MINUTES = DAY_MINUTES * 365

const val MINUTE_SECONDS = 60
const val HOUR_SECONDS = HOUR_MINUTES * 60
const val DAY_SECONDS = DAY_MINUTES * 60
const val WEEK_SECONDS = WEEK_MINUTES * 60
const val MONTH_SECONDS = MONTH_MINUTES * 60
const val YEAR_SECONDS = YEAR_MINUTES * 60

// shared preferences
const val PREFS_KEY = "Prefs"
const val APP_RUN_COUNT = "app_run_count"
const val LAST_VERSION = "last_version"

// sorting
const val SORT_ORDER = "sort_order"
const val SORT_BY_NAME = 1
const val SORT_BY_DATE_MODIFIED = 2
const val SORT_BY_SIZE = 4
const val SORT_BY_DATE_CREATED = 8
const val SORT_BY_EXTENSION = 16
const val SORT_BY_FOLDERNAME = 32
const val SORT_DESCENDING = 64

// permissions
const val PERMISSION_READ_STORAGE = 1
const val PERMISSION_WRITE_STORAGE = 2
const val PERMISSION_CAMERA = 3
const val PERMISSION_RECORD_AUDIO = 4
const val PERMISSION_READ_CONTACTS = 5
const val PERMISSION_WRITE_CONTACTS = 6
const val PERMISSION_READ_CALENDAR = 7
const val PERMISSION_WRITE_CALENDAR = 8
const val PERMISSION_CALL_PHONE = 9
const val PERMISSION_READ_CALL_LOG = 10
const val PERMISSION_WRITE_CALL_LOG = 11
const val PERMISSION_GET_ACCOUNTS = 12

val photoExtensions: Array<String> get() = arrayOf(".jpg", ".png", ".jpeg", ".bmp", ".webp", ".heic", ".heif")
val videoExtensions: Array<String> get() = arrayOf(".mp4", ".mkv", ".webm", ".avi", ".3gp", ".mov", ".m4v", ".3gpp")
val audioExtensions: Array<String> get() = arrayOf(".mp3", ".wav", ".wma", ".ogg", ".m4a", ".opus", ".flac", ".aac")
val rawExtensions: Array<String> get() = arrayOf(".dng", ".orf", ".nef", ".arw", ".rw2", ".cr2", ".cr3")

const val DATE_FORMAT_ONE = "dd.MM.yyyy"
const val DATE_FORMAT_TWO = "dd/MM/yyyy"
const val DATE_FORMAT_THREE = "MM/dd/yyyy"
const val DATE_FORMAT_FOUR = "yyyy-MM-dd"

const val TIME_FORMAT_12 = "hh:mm a"
const val TIME_FORMAT_24 = "HH:mm"

const val MONDAY_BIT = 1
const val TUESDAY_BIT = 2
const val WEDNESDAY_BIT = 4
const val THURSDAY_BIT = 8
const val FRIDAY_BIT = 16
const val SATURDAY_BIT = 32
const val SUNDAY_BIT = 64
const val EVERY_DAY_BIT = MONDAY_BIT or TUESDAY_BIT or WEDNESDAY_BIT or THURSDAY_BIT or FRIDAY_BIT or SATURDAY_BIT or SUNDAY_BIT
const val WEEK_DAYS_BIT = MONDAY_BIT or TUESDAY_BIT or WEDNESDAY_BIT or THURSDAY_BIT or FRIDAY_BIT
const val WEEKENDS_BIT = SATURDAY_BIT or SUNDAY_BIT

fun isOnMainThread() = Looper.myLooper() == Looper.getMainLooper()

fun ensureBackgroundThread(callback: () -> Unit) {
    if (isOnMainThread()) {
        Thread {
            callback()
        }.start()
    } else {
        callback()
    }
}

fun isMarshmallowPlus() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
fun isNougatPlus() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.N
fun isNougatMR1Plus() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1
fun isOreoPlus() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.O
fun isPiePlus() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.P

fun getDateFormats() = arrayListOf(
        "yyyy-MM-dd",
        "yyyyMMdd",
        "yyyy.MM.dd",
        "yy-MM-dd",
        "yyMMdd",
        "yy.MM.dd",
        "yy/MM/dd",
        "MM-dd",
        "--MM-dd",
        "MMdd",
        "MM/dd",
        "MM.dd"
)

val normalizeRegex = "\\p{InCombiningDiacriticalMarks}+".toRegex()