package com.smttcn.commons.extensions

import android.app.Activity
import android.content.*
import android.net.Uri
import com.smttcn.safebox.R
//import com.smttcn.commons.dialogs.*
import com.smttcn.commons.helpers.*
import com.smttcn.safebox.MyApplication
//import kotlinx.android.synthetic.main.dialog_title.view.*

fun Activity.appLaunched(appId: String) {
    MyApplication.baseConfig.appId = appId
    if (MyApplication.baseConfig.appRunCount == 0) {
        // first run
    }
    MyApplication.baseConfig.appRunCount++
}

fun Activity.launchViewIntent(id: Int) = launchViewIntent(getString(id))

fun Activity.launchViewIntent(url: String) {
    ensureBackgroundThread {
        Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
            if (resolveActivity(packageManager) != null) {
                startActivity(this)
            } else {
                toast(R.string.no_app_found)
            }
        }
    }
}

//fun Activity.hideKeyboard() {
//    val inputMethodManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
//    inputMethodManager.hideSoftInputFromWindow((currentFocus ?: View(this)).windowToken, 0)
//    window!!.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN)
//    currentFocus?.clearFocus()
//}
//
//fun Activity.showKeyboard(et: EditText) {
//    et.requestFocus()
//    val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
//    imm.showSoftInput(et, InputMethodManager.SHOW_IMPLICIT)
//}
//
//fun Activity.hideKeyboard(view: View) {
//    val inputMethodManager = getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
//    inputMethodManager.hideSoftInputFromWindow(view.windowToken, 0)
//}
//
fun Activity.isSimilarByteArray(data1: ByteArray, data2: ByteArray) : Boolean {
    return data1.toString(Charsets.UTF_8).equals(data2.toString(Charsets.UTF_8))
}
