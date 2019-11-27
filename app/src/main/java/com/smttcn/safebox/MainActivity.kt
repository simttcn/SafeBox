package com.smttcn.safebox

import android.content.Intent
import android.os.Bundle
import android.text.InputType
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
import com.smttcn.commons.helpers.INTERVAL_BACK_BUTTON_QUIT_IN_MS
import com.smttcn.commons.helpers.REQUEST_CODE_NEW_PASSWORD
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
                R.id.navigation_folder, R.id.navigation_image, R.id.navigation_settings
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
        startActivity(intent)
        finishAffinity()
    }

}
