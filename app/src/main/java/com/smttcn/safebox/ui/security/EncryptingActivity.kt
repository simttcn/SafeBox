package com.smttcn.safebox.ui.security

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Parcelable
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.Button
import android.widget.EditText
import com.afollestad.materialdialogs.MaterialDialog
import com.smttcn.commons.Manager.FileManager
import com.smttcn.commons.activities.BaseActivity
import com.smttcn.commons.extensions.showKeyboard
import com.smttcn.commons.helpers.Authenticator
import com.smttcn.commons.helpers.INTENT_SHARE_FILE_URI
import com.smttcn.commons.helpers.MIN_PASSWORD_LENGTH
import com.smttcn.safebox.R
import java.io.File

class EncryptingActivity: BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        initActivity()
        initActivityUI()
    }

    override fun onStop() {
        super.onStop()
        setResult(Activity.RESULT_CANCELED)
        finish()
    }

    private fun initActivity() {
        setContentView(R.layout.activity_encrypting)
    }

    private fun initActivityUI() {
        val EncryptingPassword = findViewById<EditText>(R.id.encrypting_password)
        val ConfirmPassword = findViewById<EditText>(R.id.confirm_password)
        val okButton = findViewById<Button>(R.id.ok)
        val CancelButton = findViewById<Button>(R.id.cancel)

        EncryptingPassword.addTextChangedListener(object: TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                toEnableOkButton(s.toString(), ConfirmPassword.text.toString())
            }

        })

        ConfirmPassword.addTextChangedListener(object: TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                toEnableOkButton(EncryptingPassword.text.toString(), s.toString())
            }

        })

        CancelButton.setOnClickListener {
            // user cancel
            setResult(Activity.RESULT_CANCELED)
            finish()
        }

        okButton.setOnClickListener {
            if (isEncryptingPasswordValid(EncryptingPassword.text.toString(), ConfirmPassword.text.toString())){

                var encryptedSuccess = false

                var fileUri = intent.getParcelableExtra<Parcelable>(INTENT_SHARE_FILE_URI) as Uri
                var encryptedFilePath = encryptFileFromURI(EncryptingPassword.text.toString().toCharArray(), fileUri)

                if (FileManager.isFileExist(encryptedFilePath)
                    && FileManager.isEncryptedFile(File(encryptedFilePath)))
                    encryptedSuccess = true

                if (encryptedSuccess) {
                    // succesfully encrypted file
                    setResult(Activity.RESULT_OK)
                    finish()

                } else {
                    // fail to encrypt file
                    setResult(Activity.RESULT_CANCELED)
                    finish()

                }

            }
        }

        showKeyboard(EncryptingPassword)

    }

    private fun encryptFileFromURI(pwd: CharArray, uri: Uri): String {

        return FileManager.encryptFileFromUriToFolder(contentResolver, pwd, uri)

    }

    private fun showMessageDialog(stringID: Int, callback: () -> Unit){
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

    private fun toEnableOkButton(encryptingPassword: String, confirmPassword: String) {
        val okButton = findViewById<Button>(R.id.ok)
        okButton.isEnabled = isEncryptingPasswordValid(encryptingPassword, confirmPassword)
    }

    private fun isEncryptingPasswordValid(encryptingPassword: String, confirmPassword: String) : Boolean {
        return (encryptingPassword.length >= MIN_PASSWORD_LENGTH
                && confirmPassword.length >= MIN_PASSWORD_LENGTH
                && encryptingPassword.equals(confirmPassword, false))
    }

}