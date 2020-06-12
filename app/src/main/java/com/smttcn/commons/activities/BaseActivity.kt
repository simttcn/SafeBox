package com.smttcn.commons.activities

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.smttcn.commons.extensions.*
import com.smttcn.commons.helpers.INTENT_CALL_FROM_MAINACTIVITY
import com.smttcn.safebox.MyApplication
import com.smttcn.safebox.ui.security.LoginActivity
import com.smttcn.safebox.ui.main.MainActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
import java.io.File
import java.io.FileInputStream





abstract class BaseActivity : AppCompatActivity(), CoroutineScope by MainScope() {
    var actionOnPermission: ((granted: Boolean) -> Unit)? = null
    var isAskingPermissions = false
    var authenticationException = false
    var isToCreatePassword = false
    private var sessionDepth = 0
    private val GENERIC_PERM_HANDLER = 100

    companion object {
        //var funAfterSAFPermission: ((success: Boolean) -> Unit)? = null
    }

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

        if (!authenticationException
            && !isToCreatePassword
            && MyApplication.getBaseConfig().enableAppPassword == true
            && !MyApplication.isAuthenticated()) {
            performAuthentication()
        }
    }

    override fun onStart() {
        super.onStart()
        sessionDepth++
        if (sessionDepth == 1) {
            //app came to foreground;
        }
    }

    override fun onStop() {
        super.onStop()
        actionOnPermission = null
        if (sessionDepth > 0)
            sessionDepth--;
        if (sessionDepth == 0) {
            // app went to background
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        //funAfterSAFPermission = null
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, resultData: Intent?) {
        super.onActivityResult(requestCode, resultCode, resultData)
    }

    fun performAuthentication() {
        val intent = Intent(this, LoginActivity::class.java)
        //intent.putExtra(INTENT_CALL_FROM_MAINACTIVITY, "yes")
        startActivity(intent)
        //finishAffinity()
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

    fun getFileInputStreamSync(path: String) = FileInputStream(File(path))

}
