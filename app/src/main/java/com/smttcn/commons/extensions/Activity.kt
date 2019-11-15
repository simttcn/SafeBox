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
import com.smttcn.commons.activities.BaseSimpleActivity
import com.smttcn.commons.crypto.Encryption
import com.smttcn.commons.crypto.Hashing
//import com.smttcn.commons.dialogs.*
import com.smttcn.commons.helpers.*
import com.smttcn.materialdialogs.MaterialDialog
import com.smttcn.materialdialogs.ModalDialog
import com.smttcn.materialdialogs.customview.customView
import com.smttcn.materialdialogs.customview.getCustomView
import com.smttcn.materialdialogs.input.input
import com.smttcn.materialdialogs.lifecycle.lifecycleOwner
//import kotlinx.android.synthetic.main.dialog_title.view.*
import java.io.*

fun AppCompatActivity.updateActionBarTitle(text: String, color: Int = baseConfig.primaryColor) {
    supportActionBar?.title = HtmlCompat.fromHtml("<font color='${color.getContrastColor().toHex()}'>$text</font>", HtmlCompat.FROM_HTML_MODE_LEGACY)
}

fun AppCompatActivity.updateActionBarSubtitle(text: String) {
    supportActionBar?.subtitle = HtmlCompat.fromHtml("<font color='${baseConfig.primaryColor.getContrastColor().toHex()}'>$text</font>", HtmlCompat.FROM_HTML_MODE_LEGACY)
}

fun Activity.appLaunched(appId: String) {
    baseConfig.appId = appId
    if (baseConfig.appRunCount == 0) {
        // first run
    }
    baseConfig.appRunCount++

    // nagging user here for upgrading (maybe...)
//    if (baseConfig.appRunCount % 50 == 0 && !isAProApp()) {
//        showDonateOrUpgradeDialog()
//    }

}

fun Activity.handleAppPasswordProtection(callback: (success: Boolean) -> Unit) {
    // Todo:
//    SecurityDialog(this, baseConfig.appPasswordHash, baseConfig.appProtectionType) { hash, type, success ->
//        callback(success)
//    }
}

//fun Activity.showDonateOrUpgradeDialog() {
//    if (getCanAppBeUpgraded()) {
//        UpgradeToProDialog(this)
//    } else if (!baseConfig.hadThankYouInstalled && !isThankYouInstalled()) {
//        DonateDialog(this)
//    }
//}

fun BaseSimpleActivity.isShowingSAFDialog(path: String): Boolean {
//    return if (isPathOnSD(path) && (baseConfig.treeUri.isEmpty() || !hasProperStoredTreeUri(false))) {
//        runOnUiThread {
//            if (!isDestroyed && !isFinishing) {
//                WritePermissionDialog(this, false) {
//                    Intent(Intent.ACTION_OPEN_DOCUMENT_TREE).apply {
//                        putExtra("android.content.extra.SHOW_ADVANCED", true)
//                        if (resolveActivity(packageManager) == null) {
//                            type = "*/*"
//                        }
//
//                        if (resolveActivity(packageManager) != null) {
//                            checkedDocumentPath = path
//                            startActivityForResult(this, OPEN_DOCUMENT_TREE)
//                        } else {
//                            toast(R.string.unknown_error_occurred)
//                        }
//                    }
//                }
//            }
//        }
//        true
//    } else {
//        false
//    }
    return false
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

fun BaseSimpleActivity.getFileInputStreamSync(path: String) = FileInputStream(File(path))

fun Activity.performAppAuthentication(callback: (success: Boolean) -> Unit) {
    MaterialDialog(this).show {
        title(R.string.enter_password)
        input(
            hint = getString(R.string.enter_your_password),
            inputType = InputType.TYPE_CLASS_TEXT + InputType.TYPE_TEXT_VARIATION_PASSWORD
        ) { _, text ->

            val authenticator: Authenticator = Authenticator(context)
            authenticator.authenticateAppPassword(text.toString(), callback)
        }

        negativeButton(android.R.string.cancel) { finish()}
        positiveButton(android.R.string.ok)
    }
}

fun Activity.performNewAppPassword(parent: AppCompatActivity, callback: (success: Boolean) -> Unit) {

    val dialog = MaterialDialog(this, ModalDialog).show {
        title(R.string.new_password)
        customView(R.layout.view_password_new, scrollable = true, horizontalPadding = true)
        positiveButton(android.R.string.ok) { dialog ->
            // Pull the password out of the custom view when the positive button is pressed
            val passwordNewInput: EditText = dialog.getCustomView().findViewById(R.id.new_password)
            val passwordConfirmInput: EditText = dialog.getCustomView().findViewById(R.id.confirm_password)

            val authenticator: Authenticator = Authenticator(context)
            authenticator.newAppPassword(passwordNewInput.text.toString(), callback)

        }
        negativeButton(android.R.string.cancel)
        lifecycleOwner(parent)
    }

    // Setup custom view content
    val customView = dialog.getCustomView()
    val passwordNewInput: EditText = customView.findViewById(R.id.new_password)
    passwordNewInput.inputType = InputType.TYPE_CLASS_TEXT + InputType.TYPE_TEXT_VARIATION_PASSWORD
    passwordNewInput.transformationMethod =  PasswordTransformationMethod.getInstance()
    val passwordConfirmInput: EditText = customView.findViewById(R.id.confirm_password)
    passwordConfirmInput.inputType = InputType.TYPE_CLASS_TEXT + InputType.TYPE_TEXT_VARIATION_PASSWORD
    passwordConfirmInput.transformationMethod =  PasswordTransformationMethod.getInstance()

}

fun Activity.performChangeAppPassword(callback: (success: Boolean) -> Unit) {
    MaterialDialog(this).show {
        title(R.string.enter_password)
        input(
            hint = getString(R.string.enter_your_password),
            inputType = InputType.TYPE_CLASS_TEXT + InputType.TYPE_TEXT_VARIATION_PASSWORD
        ) { _, text ->
            //toast("Input: $text")

            // check entered password here
            // return result
            callback(true)
        }
        negativeButton(android.R.string.cancel) { finish()}
        positiveButton(android.R.string.ok)
    }
}

fun Activity.isSimilarByteArray(data1: ByteArray, data2: ByteArray) : Boolean {
    return data1.toString(Charsets.UTF_8).equals(data2.toString(Charsets.UTF_8))
}
