package com.smttcn.safebox

import android.os.Bundle
import android.text.InputType
import android.widget.EditText
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.smttcn.commons.crypto.KeyStoreUtil
import com.smttcn.commons.extensions.performAppAuthentication
import com.smttcn.commons.extensions.performNewAppPassword
import com.smttcn.commons.extensions.toast
import com.smttcn.commons.helpers.KEY_STORE_ALIAS
import com.smttcn.materialdialogs.MaterialDialog
import com.smttcn.materialdialogs.input.input
import timber.log.Timber

class MainActivity : AppCompatActivity() {

    companion object {
        var isAuthenticated = false;
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        isAuthenticated = false

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
        isAuthenticated = false

        //performCreatePassword()
        performAuthentication()
    }

    fun performAuthentication() {
        performAppAuthentication {
            if (it) {
                isAuthenticated = true
                toast("Authentication OKAY!")
            } else {
                isAuthenticated = false
                toast("Authentication FAILED!")
            }
        }
    }

    fun performCreatePassword() {
        performNewAppPassword(this) {
            if (it) {
                isAuthenticated = true
                toast("Authentication OKAY!")
            } else {
                isAuthenticated = false
                toast("Authentication FAILED!")
            }
        }
    }

}
