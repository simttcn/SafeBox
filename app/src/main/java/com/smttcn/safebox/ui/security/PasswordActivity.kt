package com.smttcn.safebox.ui.security

import android.app.Activity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.Button
import android.widget.EditText
import com.smttcn.commons.activities.BaseActivity
import com.smttcn.commons.extensions.showKeyboard
import com.smttcn.commons.helpers.Authenticator
import com.smttcn.commons.helpers.MIN_PASSWORD_LENGTH
import com.afollestad.materialdialogs.MaterialDialog
import com.smttcn.commons.Manager.FileManager
import com.smttcn.commons.helpers.INTENT_CALL_FROM_MAINACTIVITY
import com.smttcn.commons.helpers.INTENT_TO_CREATE_APP_PASSWORD
import com.smttcn.safebox.MyApplication
import com.smttcn.safebox.ui.main.MainActivity
import com.smttcn.safebox.R


class PasswordActivity : BaseActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // get param from bundle
        val bundle = intent.extras
        val param1 = bundle?.getString(INTENT_TO_CREATE_APP_PASSWORD)
        if (param1.equals("yes")) isToCreatePassword = true

        // Password exists but not authenticated, then quit the app immediately
        if (!isToCreatePassword && !MyApplication.authenticated) finishAndRemoveTask()

        initActivity()
        initActivityUI()
    }

    override fun onStop() {
        super.onStop()
        setResult(Activity.RESULT_CANCELED)
        finish()
    }

    private fun initActivity() {
        setContentView(R.layout.activity_password)
    }

    private fun initActivityUI() {

        val NewPassword = findViewById<EditText>(R.id.new_password)
        val ConfirmPassword = findViewById<EditText>(R.id.confirm_password)
        val ConfirmButton = findViewById<Button>(R.id.confirm)
        val CancelButton = findViewById<Button>(R.id.cancel)

        if (!isToCreatePassword) {
            // user want to change the app password
            val CurrentPassword = findViewById<EditText>(R.id.existing_password)

            CurrentPassword.visibility = if (!isToCreatePassword) View.VISIBLE else View.GONE

            showKeyboard(CurrentPassword)
        }

        NewPassword.addTextChangedListener(object: TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                toEnableConfirmButton(s.toString(), ConfirmPassword.text.toString())
            }

        })

        ConfirmPassword.addTextChangedListener(object: TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                toEnableConfirmButton(NewPassword.text.toString(), s.toString())
            }

        })

        CancelButton.setOnClickListener {
            setResult(Activity.RESULT_CANCELED)
            finish()
        }

        ConfirmButton.setOnClickListener {
            if (isNewPasswordValid(NewPassword.text.toString(), ConfirmPassword.text.toString())){

                val authenticator: Authenticator = Authenticator()

                if (isToCreatePassword) {
                    // create new password on first app entry
                    authenticator.newAppPassword(NewPassword.text.toString()){
                        if (it){
                            // new password hash created successfully
                            setResult(Activity.RESULT_OK)
                            finish()
                        } else {
                            // error creating new password hash
                            setResult(Activity.RESULT_CANCELED)
                            finish()
                        }
                    }

                } else {
                    // change password
                    val CurrentPassword = findViewById<EditText>(R.id.existing_password)

                    if (CurrentPassword.text.toString().length >= MIN_PASSWORD_LENGTH) {
                        authenticator.authenticateAppPassword(CurrentPassword.text.toString()) {
                            if (it == true) {
                                // correct app password, so go ahead to change the password
                                authenticator.changeAppPassword(
                                    CurrentPassword.text.toString(),
                                    NewPassword.text.toString()) {

                                    if (it == true) {
                                        CurrentPassword.text.clear()
                                        NewPassword.text.clear()

                                        setResult(Activity.RESULT_OK)
                                        finish()
                                    } else {
                                        showChangePasswordDialog(R.string.dlg_msg_change_app_password_error) {
                                            CurrentPassword.selectAll()
                                            showKeyboard(CurrentPassword)
                                        }
                                    }
                                }




                            } else {
                                // incorrect app password
                                showChangePasswordDialog(R.string.dlg_msg_change_app_password_incorrect) {
                                    CurrentPassword.selectAll()
                                    showKeyboard(CurrentPassword)
                                }
                            }
                        }
                    } else {
                        showChangePasswordDialog(R.string.dlg_msg_change_app_password_incorrect) {
                            CurrentPassword.selectAll()
                            showKeyboard(CurrentPassword)
                        }
                    }

                }
            }
        }

        if (isToCreatePassword) {
            showKeyboard(NewPassword)
        }
    }

    private fun showChangePasswordDialog(stringID: Int, callback: () -> Unit){
        MaterialDialog(this).show {
            title(R.string.dlg_title_change_app_password)
            message(stringID)
            positiveButton(R.string.btn_ok)
            cancelable(false)  // calls setCancelable on the underlying dialog
            cancelOnTouchOutside(false)  // calls setCanceledOnTouchOutside on the underlying dialog
        }.positiveButton {
            callback()
        }
    }

    private fun toEnableConfirmButton(newPassword: String, confirmPassword: String) {
        val confirmButton = findViewById<Button>(R.id.confirm)
        confirmButton.isEnabled = isNewPasswordValid(newPassword, confirmPassword)
    }


    private fun isNewPasswordValid(newPassword: String, confirmPassword: String) : Boolean {
        return (newPassword.length >= MIN_PASSWORD_LENGTH
                && confirmPassword.length >= MIN_PASSWORD_LENGTH
                && newPassword.equals(confirmPassword, false))
    }


}
