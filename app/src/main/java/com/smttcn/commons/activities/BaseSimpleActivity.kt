package com.smttcn.commons.activities

import android.app.Activity
import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.provider.DocumentsContract
import android.view.Menu
import android.view.MenuItem
import android.view.WindowManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.util.Pair
import com.smttcn.safebox.R
import com.smttcn.commons.extensions.*
import com.smttcn.commons.helpers.*
import java.io.File
import java.util.*
import java.util.regex.Pattern

abstract class BaseSimpleActivity : AppCompatActivity() {
    var actionOnPermission: ((granted: Boolean) -> Unit)? = null
    var isAskingPermissions = false
    var checkedDocumentPath = ""

    private val GENERIC_PERM_HANDLER = 100

    companion object {
        var funAfterSAFPermission: ((success: Boolean) -> Unit)? = null
    }

    abstract fun getAppIconIDs(): ArrayList<Int>

    abstract fun getAppLauncherName(): String

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)

        // Disabling the piracy check routine
//        if (!packageName.startsWith("com.simtetchun.", true)) {
//            if ((0..50).random() == 10 || baseConfig.appRunCount % 100 == 0) {
//                val label = "You are using a fake version of the app. For your own safety download the original one from www.simplemobiletools.com. Thanks"
//                ConfirmationDialog(this, label, positive = R.string.ok, negative = 0) {
//                    launchViewIntent("https://play.google.com/store/apps/dev?id=9070296388022589266")
//                }
//            }
//        }
    }

    override fun onResume() {
        super.onResume()
    }

    override fun onStop() {
        super.onStop()
        actionOnPermission = null
    }

    override fun onDestroy() {
        super.onDestroy()
        funAfterSAFPermission = null
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> finish()
            else -> return super.onOptionsItemSelected(item)
        }
        return true
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, resultData: Intent?) {
        super.onActivityResult(requestCode, resultCode, resultData)
    }

//    fun startAboutActivity(appNameId: Int, licenseMask: Int, versionName: String, faqItems: ArrayList<FAQItem>, showFAQBeforeMail: Boolean) {
//        Intent(applicationContext, AboutActivity::class.java).apply {
//            putExtra(APP_ICON_IDS, getAppIconIDs())
//            putExtra(APP_LAUNCHER_NAME, getAppLauncherName())
//            putExtra(APP_NAME, getString(appNameId))
//            putExtra(APP_LICENSES, licenseMask)
//            putExtra(APP_VERSION_NAME, versionName)
//            putExtra(APP_FAQ, faqItems)
//            putExtra(SHOW_FAQ_BEFORE_MAIL, showFAQBeforeMail)
//            startActivity(this)
//        }
//    }

    fun handleSAFDialog(path: String, callback: (success: Boolean) -> Unit): Boolean {
        return if (!packageName.startsWith("com.simtetchuns")) {
            callback(true)
            false
        } else if (isShowingSAFDialog(path)) {
            funAfterSAFPermission = callback
            true
        } else {
            callback(true)
            false
        }
    }

    fun handlePermission(permissionId: Int, callback: (granted: Boolean) -> Unit) {
        actionOnPermission = null
        if (hasPermission(permissionId)) {
            callback(true)
        } else {
            isAskingPermissions = true
            actionOnPermission = callback
            ActivityCompat.requestPermissions(this, arrayOf(getPermissionString(permissionId)), GENERIC_PERM_HANDLER)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        isAskingPermissions = false
        if (requestCode == GENERIC_PERM_HANDLER && grantResults.isNotEmpty()) {
            actionOnPermission?.invoke(grantResults[0] == 0)
        }
    }

}
