package com.smttcn.commons.extensions

//import com.smttcn.commons.views.*
import android.Manifest
import android.app.Activity
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.graphics.drawable.Drawable
import android.os.Handler
import android.os.Looper
import android.os.Parcelable
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.Toast
import androidx.annotation.DrawableRes
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.startActivity
import androidx.core.content.FileProvider
import com.afollestad.materialdialogs.MaterialDialog
import com.smttcn.commons.helpers.*
import com.smttcn.safebox.MyApplication
import com.smttcn.safebox.R
import java.io.File
import java.util.*


fun Context.toast(id: Int, length: Int = Toast.LENGTH_SHORT) {
    toast(getString(id), length)
}


fun Context.toast(msg: String, length: Int = Toast.LENGTH_SHORT) {
    try {
        if (isOnMainThread()) {
            doToast(this, msg, length)
        } else {
            Handler(Looper.getMainLooper()).post {
                doToast(this, msg, length)
            }
        }
    } catch (e: Exception) {
    }
}


private fun doToast(context: Context, message: String, length: Int) {
    if (context is Activity) {
        if (!context.isFinishing && !context.isDestroyed) {
            Toast.makeText(context, message, length).show()
        }
    } else {
        Toast.makeText(context, message, length).show()
    }
}


fun showMessageDialog(context: Context, title: String, message: String, callback: () -> Unit){
    MaterialDialog(context).show {
        title(text = title)
        message(text = message)
        positiveButton(R.string.btn_ok)
        cancelable(false)  // calls setCancelable on the underlying dialog
        cancelOnTouchOutside(false)  // calls setCanceledOnTouchOutside on the underlying dialog
    }.positiveButton {
        callback()
    }
}


fun showMessageDialog(context: Context, titleID: Int, messageID: Int, callback: () -> Unit){
    MaterialDialog(context).show {
        title(titleID)
        message(messageID)
        positiveButton(R.string.btn_ok)
        cancelable(false)  // calls setCancelable on the underlying dialog
        cancelOnTouchOutside(false)  // calls setCanceledOnTouchOutside on the underlying dialog
    }.positiveButton {
        callback()
    }
}


fun Context.showErrorToast(msg: String, length: Int = Toast.LENGTH_LONG) {
    toast(String.format(getString(R.string.an_error_occurred), msg), length)
}


fun Context.showErrorToast(exception: Exception, length: Int = Toast.LENGTH_LONG) {
    showErrorToast(exception.toString(), length)
}


fun Context.isPasswordConfinedToPolicy(password: String): Boolean {
    return (password.length >= MIN_PASSWORD_LENGTH)
}


fun Context.isNewPasswordConfinedToPolicy(newPassword: String, confirmPassword: String): Boolean {
    return (newPassword.length >= MIN_PASSWORD_LENGTH
            && confirmPassword.length >= MIN_PASSWORD_LENGTH
            && newPassword.equals(confirmPassword, false))
}


fun Context.hasPermission(permId: Int) = ContextCompat.checkSelfPermission(this, getPermissionString(permId)) == PackageManager.PERMISSION_GRANTED


fun Context.getPermissionString(id: Int) = when (id) {
    PERMISSION_READ_STORAGE -> Manifest.permission.READ_EXTERNAL_STORAGE
    PERMISSION_WRITE_STORAGE -> Manifest.permission.WRITE_EXTERNAL_STORAGE
    PERMISSION_CAMERA -> Manifest.permission.CAMERA
    PERMISSION_RECORD_AUDIO -> Manifest.permission.RECORD_AUDIO
    PERMISSION_READ_CONTACTS -> Manifest.permission.READ_CONTACTS
    PERMISSION_WRITE_CONTACTS -> Manifest.permission.WRITE_CONTACTS
    PERMISSION_READ_CALENDAR -> Manifest.permission.READ_CALENDAR
    PERMISSION_WRITE_CALENDAR -> Manifest.permission.WRITE_CALENDAR
    PERMISSION_CALL_PHONE -> Manifest.permission.CALL_PHONE
    PERMISSION_READ_CALL_LOG -> Manifest.permission.READ_CALL_LOG
    PERMISSION_WRITE_CALL_LOG -> Manifest.permission.WRITE_CALL_LOG
    PERMISSION_GET_ACCOUNTS -> Manifest.permission.GET_ACCOUNTS
    else -> ""
}


fun Context.getDrawableCompat(@DrawableRes drawableRes: Int): Drawable? {
    return AppCompatResources.getDrawable(this, drawableRes)
}


fun Context.showKeyboard(et: EditText) {
    et.requestFocus()
    val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    imm.showSoftInput(et, InputMethodManager.SHOW_IMPLICIT)
}


fun Context.hideKeyboard(view: View) {
    val inputMethodManager = getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
    inputMethodManager.hideSoftInputFromWindow(view.windowToken, 0)
}


fun sendShareItent(context: Context, filepath: String) {

    if (filepath.isNotEmpty()) {

        MyApplication.isSharingItem = true

        val file = File(filepath)
        val contentUri = FileProvider.getUriForFile(MyApplication.applicationContext, APP_AUTHORITY, file)
        val excludedComponents = ArrayList<ComponentName>()

        // create new Intent for getting list of related intent
        val intent = Intent(Intent.ACTION_SEND)
        intent.setDataAndType(contentUri, filepath.getMimeType())
        //intent.type = filepath.getMimeType()
        intent.putExtra(Intent.EXTRA_STREAM, contentUri)

        val resInfo: List<ResolveInfo> = context.packageManager.queryIntentActivities(intent, 0)
        // loop through all of them
        for (resolveInfo in resInfo) {
            val packageName: String = resolveInfo.activityInfo.packageName
            val name = resolveInfo.activityInfo.name
            if (packageName.contains(PACKAGE_NAME)) { // package to be excluded (self)
                excludedComponents.add(ComponentName(packageName, name))
            }
        }

        if (excludedComponents.size == resInfo.size) {
            Toast.makeText(context, context.getString(R.string.no_app_to_share), Toast.LENGTH_SHORT).show()
        } else {
            val chooserIntent = Intent.createChooser(intent , context.getString(R.string.sharing_to))
            chooserIntent.putExtra(
                Intent.EXTRA_EXCLUDE_COMPONENTS,
                excludedComponents.toTypedArray()
            )
            startActivity(context, chooserIntent, null)
        }


//        MyApplication.isSharingItem = true
//
//        val file = File(filepath)
//        val contentUri = FileProvider.getUriForFile(MyApplication.applicationContext, APP_AUTHORITY, file)
//        var targetedShareIntents: MutableList<Intent> = mutableListOf()
//
//        // create new Intent for getting list of related intent
//        val intent = Intent(Intent.ACTION_SEND)
//        // only MimeType is good enough
//        intent.type = filepath.getMimeType()
//
//        // get a list of related share intents
//        val resInfo: List<ResolveInfo> = context.packageManager.queryIntentActivities(intent, 0)
//        // loop through all of them
//        for (resolveInfo in resInfo) {
//            val packageName: String = resolveInfo.activityInfo.packageName
//            if (packageName != PACKAGE_NAME) { // Remove own share intent
//                val targetedShareIntent = Intent(Intent.ACTION_SEND)
//                targetedShareIntent.flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
//                targetedShareIntent.setDataAndType(contentUri, filepath.getMimeType())
//                targetedShareIntent.putExtra(Intent.EXTRA_STREAM, contentUri)
//                targetedShareIntent.setPackage(packageName)
//                targetedShareIntents.add(targetedShareIntent)
//            }
//        }
//
//        // create chooser intent, fill it in and start it
//        val chooserIntent = Intent.createChooser(Intent(), context.getString(R.string.sharing_to))
//        chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, targetedShareIntents.toTypedArray())
//        startActivity(context, chooserIntent, null)

    }

}





