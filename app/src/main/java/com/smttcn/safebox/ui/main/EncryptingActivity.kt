package com.smttcn.safebox.ui.main

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Parcelable
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.EditText
import com.smttcn.commons.manager.FileManager
import com.smttcn.commons.activities.BaseActivity
import com.smttcn.commons.extensions.getFilenameFromPath
import com.smttcn.commons.extensions.removeEncryptedExtension
import com.smttcn.commons.extensions.showKeyboard
import com.smttcn.commons.helpers.*
import com.smttcn.safebox.R
import kotlinx.android.synthetic.main.activity_encrypting.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.File

class EncryptingActivity: BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        initActivity()
        initActivityUI()

        showProgressBar(false)
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
        val btnOk = findViewById<Button>(R.id.ok)
        val btnCancel = findViewById<Button>(R.id.cancel)

        showKeyboard(EncryptingPassword)


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

        btnCancel.setOnClickListener {
            // user cancel
            setResult(Activity.RESULT_CANCELED)
            finish()
        }

        btnOk.setOnClickListener {
            if (isEncryptingPasswordValid(EncryptingPassword.text.toString(), ConfirmPassword.text.toString())){

                showProgressBar(true)

                GlobalScope.launch(Dispatchers.IO) {

                    var encryptedSuccess = false

                    var fileUri = intent.getParcelableExtra<Parcelable>(INTENT_SHARE_FILE_URI) as Uri
                    var encryptedFilePath = encryptFileFromURI(EncryptingPassword.text.toString().toCharArray(), fileUri)

                    if (FileManager.isFileExist(encryptedFilePath)
                        && FileManager.isEncryptedFile(File(encryptedFilePath)))
                        encryptedSuccess = true

                    launch(Dispatchers.Main) {

                        showProgressBar(false)

                        if (encryptedSuccess) {
                            // succesfully encrypted file
                            var resultIntent = Intent()
                            resultIntent.putExtra(INTENT_ENCRYPTED_FILENAME, encryptedFilePath.getFilenameFromPath().removeEncryptedExtension())
                            setResult(Activity.RESULT_OK, resultIntent)
                            finish()

                        } else {
                            // fail to encrypt file
                            setResult(INTENT_RESULT_FAILED)
                            finish()

                        }

                    }
                }

            }
        }

    }

    private fun encryptFileFromURI(pwd: CharArray, uri: Uri): String {

        return FileManager.encryptFileFromUriToFolder(contentResolver, pwd, uri)

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

    fun showProgressBar(show: Boolean) {
        if (show) {
            window.setFlags(
                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)

            encryptingActivityProgressBarContainer.visibility = View.VISIBLE
        } else {
            window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)

            encryptingActivityProgressBarContainer.visibility = View.GONE
        }
    }



}