package com.smttcn.safebox

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.method.MovementMethod
import android.text.method.ScrollingMovementMethod
import android.util.AttributeSet
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.smttcn.commons.extensions.toast
import com.smttcn.commons.helpers.INTENT_CALL_FROM_MAINACTIVITY
import com.smttcn.commons.helpers.INTERVAL_BACK_BUTTON_QUIT_IN_MS
import timber.log.Timber
import android.view.Menu;
import android.view.MenuItem;
import android.view.View
import android.widget.TextView
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity() {

    var BackButtonPressedOnce = false
    var LastPressedBackTime = System.currentTimeMillis()
    private lateinit var mainViewModel: MainViewModel

    companion object {
        fun isAuthenticated(): Boolean {
            return MyApplication.globalAppAuthenticated.equals("yes")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        fab.setOnClickListener { view ->
            Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                .setAction("Action", null).show()
        }

        initApp()
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

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId

        if (id == R.id.menu_settings) {
            startActivity(Intent(this@MainActivity, SettingsActivity::class.java))
            return true
        }

        return super.onOptionsItemSelected(item)
    }

    fun initApp() {
        if (BuildConfig.DEBUG) Timber.plant(Timber.DebugTree())
        MyApplication.setContext(this)

        mainViewModel = ViewModelProviders.of(this).get(MainViewModel::class.java)
        val textView: TextView = findViewById(R.id.text_home)
        textView.movementMethod = ScrollingMovementMethod()
        mainViewModel.text.observe(this, Observer {
            textView.text = it
        })
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
