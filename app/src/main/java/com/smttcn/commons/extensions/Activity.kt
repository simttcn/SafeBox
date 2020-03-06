package com.smttcn.commons.extensions

import android.app.Activity
import android.content.*
import android.net.Uri
import android.text.InputType
import android.text.method.PasswordTransformationMethod
import android.view.View
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import androidx.core.text.HtmlCompat
import com.smttcn.safebox.R
import com.smttcn.commons.crypto.Encryption
import com.smttcn.commons.crypto.Hashing
//import com.smttcn.commons.dialogs.*
import com.smttcn.commons.helpers.*
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.ModalDialog
import com.afollestad.materialdialogs.customview.customView
import com.afollestad.materialdialogs.customview.getCustomView
import com.afollestad.materialdialogs.input.input
import com.afollestad.materialdialogs.lifecycle.lifecycleOwner
import com.smttcn.safebox.MyApplication
//import kotlinx.android.synthetic.main.dialog_title.view.*
import java.io.*

fun Activity.appLaunched(appId: String) {
    MyApplication.getBaseConfig().appId = appId
    if (MyApplication.getBaseConfig().appRunCount == 0) {
        // first run
    }
    MyApplication.getBaseConfig().appRunCount++
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

fun Activity.hideKeyboard() {
    val inputMethodManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    inputMethodManager.hideSoftInputFromWindow((currentFocus ?: View(this)).windowToken, 0)
    window!!.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN)
    currentFocus?.clearFocus()
}

fun Activity.showKeyboard(et: EditText) {
    et.requestFocus()
    val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    imm.showSoftInput(et, InputMethodManager.SHOW_IMPLICIT)
}

fun Activity.hideKeyboard(view: View) {
    val inputMethodManager = getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
    inputMethodManager.hideSoftInputFromWindow(view.windowToken, 0)
}

fun Activity.isSimilarByteArray(data1: ByteArray, data2: ByteArray) : Boolean {
    return data1.toString(Charsets.UTF_8).equals(data2.toString(Charsets.UTF_8))
}
