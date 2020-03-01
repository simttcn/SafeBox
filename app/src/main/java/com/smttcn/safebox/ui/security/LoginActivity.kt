package com.smttcn.safebox.ui.security

import android.app.Activity
import android.content.*
import android.os.Bundle
import androidx.annotation.StringRes
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.*
import com.smttcn.commons.activities.BaseActivity
import com.smttcn.commons.crypto.KeyUtil
import com.smttcn.commons.helpers.Authenticator
import com.smttcn.commons.helpers.INTENT_CALL_FROM_MAINACTIVITY
import com.smttcn.commons.helpers.MIN_PASSWORD_LENGTH
import com.smttcn.commons.helpers.REQUEST_CODE_NEW_PASSWORD
import com.smttcn.materialdialogs.MaterialDialog
import com.smttcn.materialdialogs.callbacks.onDismiss
import com.smttcn.safebox.ui.main.MainActivity
import com.smttcn.safebox.MyApplication
import com.smttcn.safebox.R
import com.smttcn.safebox.database.AppDatabase
import com.smttcn.safebox.helpers.SampleHelper


class LoginActivity : BaseActivity() {

    private var IsCalledFromMainActivity = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initialize()
        initializeUI()

    }

    private fun initialize() {

        isLoginActivity = true

        // get param from bundle
        val bundle = intent.extras
        val param1 = bundle?.getString(INTENT_CALL_FROM_MAINACTIVITY)
        if (param1.equals("yes")) IsCalledFromMainActivity = true

        // redirect to create new password screen if necessary
        val authenticator = Authenticator()
        if (!authenticator.isAppPasswordHashExist())
            redirectToNewPasswordActivity()

        setContentView(R.layout.activity_login)

    }

    override fun onResume() {
        super.onResume()
        MyApplication.globalAppAuthenticated = "no"
    }

    private fun initializeUI() {
        val Password = findViewById<EditText>(R.id.password)
        val LoginButton = findViewById<Button>(R.id.login)

        Password.addTextChangedListener(object: TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                LoginButton.isEnabled = s.toString().length >= MIN_PASSWORD_LENGTH
            }

        })

        LoginButton.setOnClickListener {
            // togo: perform authenticaiton here
            val authenticator: Authenticator = Authenticator()
            if (Password.length() >= MIN_PASSWORD_LENGTH) {
                authenticator.authenticateAppPassword((Password.text.toString())) {
                    if (it == true) {
                        // Login successfully
                        onSuccessfulLogin(Password.text.toString().toCharArray())
                        Password.text.clear()

                        if (!IsCalledFromMainActivity) {
                            // not called from MainActivity, so start one
                            val intent = Intent(this, MainActivity::class.java)
                            startActivity(intent)
                            finishAffinity()
                        } else {
                            // called from MainActivity, so finish and return
                            finish()
                        }

//                        val Message = findViewById<TextView>(R.id.message)
//                        Message.text = "Good good"
//                        Message.visibility = View.VISIBLE
                    } else {
                        MyApplication.globalAppAuthenticated = "no"
                        val Message = findViewById<TextView>(R.id.message)
                        Message.text = getString(R.string.enter_password_error)
                        Message.visibility = View.VISIBLE
                    }
                }
            }
        }
    }

    private fun onSuccessfulLogin(password: CharArray) {
        // todo: may have to put spinner here for long processing time
        var pwd : CharArray = CharArray(password.size)
        password.copyInto(pwd, 0, 0, password.size)
        MyApplication.globalAppAuthenticated = "yes"
        val keyUtil = KeyUtil()
        val dbSecret = keyUtil.getAppDatabaseSecretWithAppPassword(pwd)
        AppDatabase.setKey(dbSecret)
        SampleHelper().Initialze(pwd)
        pwd.fill('0', 0, pwd.size)
    }

    private fun redirectToNewPasswordActivity() {
        val intent = Intent(this, PasswordActivity::class.java)
        startActivityForResult(intent, REQUEST_CODE_NEW_PASSWORD)
    }

    private fun showLoginFailed(@StringRes errorString: Int) {
        Toast.makeText(applicationContext, errorString, Toast.LENGTH_SHORT).show()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, resultData: Intent?) {
        super.onActivityResult(requestCode, resultCode, resultData)
        // Check which request we're responding to
        if (requestCode == REQUEST_CODE_NEW_PASSWORD) {
            // Make sure the request was successful
            if (resultCode == Activity.RESULT_OK) {
                // The user set a password, so will return to this activity for user to login
                MaterialDialog(this).show {
                    title(R.string.dlg_title_success)
                    message(R.string.dlg_msg_new_password_success)
                    positiveButton(R.string.btn_ok)
                    cancelable(false)  // calls setCancelable on the underlying dialog
                    cancelOnTouchOutside(false)  // calls setCanceledOnTouchOutside on the underlying dialog
                }
            } else {
                // password not set, so quit the app
                MaterialDialog(this).show {
                    title(R.string.dlg_title_error)
                    message(R.string.dlg_msg_new_password_error)
                    positiveButton(R.string.btn_ok)
                    cancelable(false)  // calls setCancelable on the underlying dialog
                    cancelOnTouchOutside(false)  // calls setCanceledOnTouchOutside on the underlying dialog
                    onDismiss {
                        finishAndRemoveTask()
                    }
                }
            }
        }
    }

}
