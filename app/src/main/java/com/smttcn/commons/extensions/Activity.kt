package com.smttcn.commons.extensions

import android.app.Activity
import android.content.*
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.graphics.drawable.ColorDrawable
import android.media.RingtoneManager
import android.net.Uri
import android.os.TransactionTooLargeException
import android.provider.DocumentsContract
import android.provider.MediaStore
import android.text.Html
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.text.HtmlCompat
import androidx.documentfile.provider.DocumentFile
import com.smttcn.safebox.R
import com.smttcn.commons.activities.BaseSimpleActivity
//import com.smttcn.commons.dialogs.*
import com.smttcn.commons.helpers.*
import com.smttcn.commons.models.*
//import kotlinx.android.synthetic.main.dialog_title.view.*
import java.io.*
import java.util.*
import kotlin.collections.HashMap

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
