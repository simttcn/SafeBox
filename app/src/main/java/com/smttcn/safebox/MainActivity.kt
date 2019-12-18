package com.smttcn.safebox

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.view.View
import android.widget.EditText
import android.widget.Toast
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.smttcn.commons.crypto.KeyStoreUtil
import com.smttcn.commons.extensions.toast
import com.smttcn.commons.helpers.INTENT_CALL_FROM_MAINACTIVITY
import com.smttcn.commons.helpers.INTERVAL_BACK_BUTTON_QUIT_IN_MS
import com.smttcn.commons.helpers.REQUEST_CODE_CHANGE_PASSWORD
import com.smttcn.commons.helpers.REQUEST_CODE_NEW_PASSWORD
import com.smttcn.materialdialogs.MaterialDialog
import com.smttcn.materialdialogs.callbacks.onDismiss
import timber.log.Timber
import java.time.LocalDateTime
import java.util.*

class MainActivity : AppCompatActivity() {

    var BackButtonPressedOnce = false
    var LastPressedBackTime = System.currentTimeMillis()

    companion object {
        fun isAuthenticated(): Boolean {
            return MyApplication.globalAppAuthenticated.equals("yes")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        MyApplication.setContext(this)

        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }

        setContentView(R.layout.activity_main)
        val navView: BottomNavigationView = findViewById(R.id.nav_view)

        val navController = findNavController(R.id.nav_host_fragment)
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        val appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.navigation_home, R.id.navigation_image, R.id.navigation_settings
            )
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)

        val keyStoreUtil = KeyStoreUtil()
        if (keyStoreUtil.isKeyExist()) keyStoreUtil.init()

    }

    override fun onResume() {
        super.onResume()

        if (!isAuthenticated()) {
            performAuthentication()
        }
    }

    override fun onBackPressed() {
        val millisecondsSinceLastPressedBack = System.currentTimeMillis() - LastPressedBackTime
        if (!BackButtonPressedOnce || millisecondsSinceLastPressedBack > INTERVAL_BACK_BUTTON_QUIT_IN_MS) {
            toast(R.string.press_back_again_to_quit, Toast.LENGTH_LONG)
            BackButtonPressedOnce = true
            LastPressedBackTime = System.currentTimeMillis()
            return
        } else {
            finishAndRemoveTask()
        }
        super.onBackPressed()
    }

    fun performAuthentication() {
        val intent = Intent(this, LoginActivity::class.java)
        intent.putExtra(INTENT_CALL_FROM_MAINACTIVITY, "yes")
        startActivity(intent)
        //finishAffinity()
    }

//    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
//        super.onActivityResult(requestCode, resultCode, data)
//        // Check which request we're responding to
//        if (requestCode == REQUEST_CODE_CHANGE_PASSWORD) {
//            // Make sure the request was successful
//            if (resultCode == Activity.RESULT_OK) {
//                // The user changed his/her app password
//                MaterialDialog(this).show {
//                    title(R.string.change_password)
//                    message(R.string.change_password_message)
//                    positiveButton(R.string.ok)
//                    cancelable(false)  // calls setCancelable on the underlying dialog
//                    cancelOnTouchOutside(false)  // calls setCanceledOnTouchOutside on the underlying dialog
//                }
//            } else if (resultCode == Activity.RESULT_CANCELED){
//                // User cancelled password change
//                MaterialDialog(this).show {
//                    title(R.string.change_password)
//                    message(R.string.change_password_cancel_message)
//                    positiveButton(R.string.ok)
//                    cancelable(false)  // calls setCancelable on the underlying dialog
//                    cancelOnTouchOutside(false)  // calls setCanceledOnTouchOutside on the underlying dialog
//                }
//            }
//        }
//    }
}
