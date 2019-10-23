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
import com.smttcn.commons.extensions.toast
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
    }

    override fun onResume() {
        super.onResume()
        isAuthenticated = false
        MaterialDialog(this).show {
            title(R.string.enter_password)
            input(
                hint = getString(R.string.enter_your_password),
                inputType = InputType.TYPE_CLASS_TEXT + InputType.TYPE_TEXT_VARIATION_PASSWORD
            ) { _, text ->
                toast("Input: $text")
                isAuthenticated = true
            }
            negativeButton(android.R.string.cancel) { finish()}
            positiveButton(android.R.string.ok)
        }

        // Todo:
//        handleAppPasswordProtection {
//            if (it) {
//                mIsPasswordProtectionPending = false
//                tryInitFileManager()
//                checkWhatsNewDialog()
//                checkIfRootAvailable()
//                checkInvalidFavorites()
//            } else {
//                finish()
//            }
//        }
    }

}
