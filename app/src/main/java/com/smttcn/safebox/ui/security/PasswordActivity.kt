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
import com.smttcn.materialdialogs.MaterialDialog
import com.smttcn.safebox.ui.main.MainActivity
import com.smttcn.safebox.R


class PasswordActivity : BaseActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val authenticator = Authenticator()
        isToCreatePassword = !authenticator.isAppPasswordHashExist()

        // Password exists but not authenticated, then quit the app immediately
        if (!isToCreatePassword && !MainActivity.isAuthenticated()) finishAndRemoveTask()

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

        if (!isToCreatePassword) {
            // user want to change the app password
            val CurrentPassword = findViewById<EditText>(R.id.existing_password)
            val CancelButton = findViewById<Button>(R.id.cancel)

            CancelButton.visibility = if (!isToCreatePassword) View.VISIBLE else View.GONE
            CurrentPassword.visibility = if (!isToCreatePassword) View.VISIBLE else View.GONE

            CancelButton.setOnClickListener {
                setResult(Activity.RESULT_CANCELED)
                finish()
            }

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

                    if (CurrentPassword.text.toString().length > 5) {
                        authenticator.authenticateAppPassword(CurrentPassword.text.toString()) {
                            if (it == true) {
                                // correct app password, so go ahead to change the password
                                authenticator.changeAppPassword(CurrentPassword.text.toString(),
                                    NewPassword.text.toString()) {

                                    if (it == true) {
                                        setResult(Activity.RESULT_OK)
                                        finish()
                                    } else {
                                        showChangePasswordDialog(R.string.change_password_error_occurs_message) {
                                            CurrentPassword.selectAll()
                                            showKeyboard(CurrentPassword)
                                        }
                                    }
                                }




                            } else {
                                // incorrect app password
                                showChangePasswordDialog(R.string.change_password_error_message) {
                                    CurrentPassword.selectAll()
                                    showKeyboard(CurrentPassword)
                                }
                            }
                        }
                    } else {
                        showChangePasswordDialog(R.string.change_password_error_message) {
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
            title(R.string.change_password)
            message(stringID)
            positiveButton(R.string.ok)
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
        return (newPassword.length > 5
                && confirmPassword.length > 5
                && newPassword.equals(confirmPassword, false))
    }


}
