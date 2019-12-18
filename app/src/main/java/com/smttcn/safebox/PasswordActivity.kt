package com.smttcn.safebox

import android.app.Activity
import android.content.Context
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import android.os.Bundle
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.Toast
import android.view.inputmethod.InputMethodManager.SHOW_IMPLICIT
import android.content.Context.INPUT_METHOD_SERVICE
import androidx.core.content.ContextCompat.getSystemService
import androidx.core.app.ComponentActivity.ExtraData
import android.icu.lang.UCharacter.GraphemeClusterBreak.T
import android.text.TextUtils.isEmpty
import android.view.inputmethod.InputMethodManager
import android.transition.Visibility
import com.smttcn.commons.extensions.showKeyboard
import com.smttcn.commons.helpers.Authenticator
import com.smttcn.commons.helpers.BaseConfig
import com.smttcn.materialdialogs.MaterialDialog


class PasswordActivity : AppCompatActivity() {

    var toCreateNewPasswordHash = false // or other values

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initialize()
        initializeUI()
    }

    override fun onStop() {
        super.onStop()
        setResult(Activity.RESULT_CANCELED)
        finish()
    }

    private fun initialize() {

        //val baseConfig: BaseConfig = BaseConfig.newInstance(this)
        // to determine which type of UI to be shown
        val authenticator = Authenticator(this)
        toCreateNewPasswordHash = !authenticator.isAppPasswordHashExist()

        setContentView(R.layout.activity_password)
    }

    private fun initializeUI() {

        val NewPassword = findViewById<EditText>(R.id.new_password)
        val ConfirmPassword = findViewById<EditText>(R.id.confirm_password)
        val ConfirmButton = findViewById<Button>(R.id.confirm)

        if (!toCreateNewPasswordHash) {
            // user want to change the app password
            val CurrentPassword = findViewById<EditText>(R.id.existing_password)
            val CancelButton = findViewById<Button>(R.id.cancel)

            CancelButton.visibility = if (!toCreateNewPasswordHash) View.VISIBLE else View.GONE
            CurrentPassword.visibility = if (!toCreateNewPasswordHash) View.VISIBLE else View.GONE

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

                val authenticator: Authenticator = Authenticator(this)

                if (toCreateNewPasswordHash) {
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
                                // todo: correct app password, so go ahead to change the password
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

        if (toCreateNewPasswordHash) {
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
