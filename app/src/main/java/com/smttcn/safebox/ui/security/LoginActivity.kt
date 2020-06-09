package com.smttcn.safebox.ui.security

import android.app.Activity
import android.content.DialogInterface
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Parcelable
import android.text.Editable
import android.text.InputType
import android.text.TextWatcher
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.StringRes
import androidx.appcompat.app.AlertDialog
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.WhichButton
import com.afollestad.materialdialogs.actions.setActionButtonEnabled
import com.afollestad.materialdialogs.callbacks.onDismiss
import com.afollestad.materialdialogs.input.getInputField
import com.afollestad.materialdialogs.input.input
import com.afollestad.materialdialogs.lifecycle.lifecycleOwner
import com.smttcn.commons.Manager.FileManager
import com.smttcn.commons.activities.BaseActivity
import com.smttcn.commons.crypto.KeyUtil
import com.smttcn.commons.helpers.*
import com.smttcn.commons.helpers.Authenticator
import com.smttcn.safebox.MyApplication
import com.smttcn.safebox.R
import com.smttcn.safebox.database.AppDatabase
import com.smttcn.safebox.helpers.SampleHelper
import com.smttcn.safebox.ui.main.MainActivity


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
        FileManager.deleteCache(this)
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
            // perform authenticaiton here
            val authenticator: Authenticator = Authenticator()
            if (Password.length() >= MIN_PASSWORD_LENGTH) {
                authenticator.authenticateAppPassword((Password.text.toString())) {
                    if (it == true) {
                        // Login successfully
                        MyApplication.setUS(Password.text.toString().toCharArray())
                        Password.text.clear()
                        if (onSuccessfulLogin()) {
                            if (!IsCalledFromMainActivity) {
                                // not called from MainActivity, so start one
                                val intent = Intent(this, MainActivity::class.java)
                                startActivity(intent)
                                finishAffinity()
                            } else {
                                // called from MainActivity, so finish and return
                                finish()
                            }
                        } else {
                            MaterialDialog(this).show {
                                title(R.string.dlg_title_error)
                                message(R.string.dlg_msg_initialization_error)
                                positiveButton(R.string.btn_ok) {
                                    finishAffinity()
                                }
                                cancelable(false)  // calls setCancelable on the underlying dialog
                                cancelOnTouchOutside(false)  // calls setCanceledOnTouchOutside on the underlying dialog
                                lifecycleOwner(this@LoginActivity)
                            }
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

    private fun onSuccessfulLogin(): Boolean {
        // todo: may have to put spinner here for long processing time
        var result: Boolean
        // set a global flag
        MyApplication.globalAppAuthenticated = "yes"
        // Only proceed if we have finished all outstanding tasks from previous session
        if (processUnfinishedTask()) {
            // handle share from sharesheet
            handleShareFrom(MyApplication.getUS())
            // get database secret and initialize the database
            val keyUtil = KeyUtil()
            val dbSecret = keyUtil.getAppDatabaseSecretWithAppPassword(MyApplication.getUS())
            AppDatabase.setKey(dbSecret)
            // do we need to crate some sample files?
            SampleHelper().Initialze(MyApplication.getUS())

            result = true
        } else {
            result = false
        }

        return result
    }

    private fun processUnfinishedTask() : Boolean {
        // we have to cater to any unfinished business from previous session
        var result = true

        // do we have unfinished reencrypting task?
        val filePaths = MyApplication.getBaseConfig().appUnfinishedReencryptFiles
        if (filePaths != null && filePaths.count() > 0) {
            // prompt for previous password to complete unfinished task
            MaterialDialog(this).show {
                title(R.string.dlg_title_unfinished_task)
                message(R.string.dlg_msg_unfinished_task_password_prompt)
                input(
                    hint = getString(R.string.dlg_msg_unfinished_task_previous_password_hint),
                    inputType = InputType.TYPE_TEXT_VARIATION_PASSWORD,
                    waitForPositiveButton = false
                ) { _, text ->
                    setActionButtonEnabled(WhichButton.POSITIVE, text.length >= MIN_PASSWORD_LENGTH)
                }
                positiveButton(R.string.btn_ok) {
                    FileManager.reencryptFiles(filePaths, it.getInputField().text.toString().toCharArray(), MyApplication.getUS())
                }
                negativeButton(R.string.btn_cancel) {
                    result = false
                }
                lifecycleOwner(this@LoginActivity)
            }

        }

        return result
    }

    private fun handleShareFrom(pwd: CharArray) {
        if (intent?.action == Intent.ACTION_SEND) {
            if (intent.type?.startsWith("image/") == true) {
                (intent.getParcelableExtra<Parcelable>(Intent.EXTRA_STREAM) as? Uri)?.let {
                    FileManager.encryptFileFromUriToFolder(contentResolver, pwd, it)
                }
            }
        }
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
