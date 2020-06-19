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
import com.smttcn.commons.extensions.appendPath
import com.smttcn.commons.extensions.showKeyboard
import com.smttcn.commons.extensions.withTrailingCharacter
import com.smttcn.commons.helpers.*
import com.smttcn.safebox.R
import java.io.File

//todo next: to create UI option for user to choose to share decrypted or as it is.

class DecryptingActivity: BaseActivity() {

    private var encryptedFileToShare = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        encryptedFileToShare = intent.getStringExtra(INTENT_SHARE_FILE_PATH)

        initActivity()
        initActivityUI()
    }

    override fun onStop() {
        super.onStop()
        setResult(Activity.RESULT_CANCELED)
        finish()
    }

    private fun initActivity() {
        setContentView(R.layout.activity_decrypting)
    }

    private fun initActivityUI() {
        val DecryptingPassword = findViewById<EditText>(R.id.decrypting_password)
        val okButton = findViewById<Button>(R.id.ok)
        val CancelButton = findViewById<Button>(R.id.cancel)

        DecryptingPassword.addTextChangedListener(object: TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                toEnableOkButton(s.toString())
            }

        })

        CancelButton.setOnClickListener {
            // user cancel
            setResult(Activity.RESULT_CANCELED)
            finish()
        }

        okButton.setOnClickListener {
            var encryptedSuccess = false

            var decryptedFilePath = decryptFileForSharing(DecryptingPassword.text.toString().toCharArray(), encryptedFileToShare)

            if (FileManager.isFileExist(decryptedFilePath))
                encryptedSuccess = true

            if (encryptedSuccess) {
                // succesfully decrypted file
                var intent = Intent()
                intent.putExtra(INTENT_SHARE_FILE_PATH, decryptedFilePath)
                setResult(Activity.RESULT_OK, intent)
                finish()
            } else {
                // fail to encrypt file
                setResult(INTENT_RESULT_FAILED)
                finish()
            }
        }

        showKeyboard(DecryptingPassword)

    }

    fun decryptFileForSharing(pwd: CharArray, filepath: String): String {

        var decryptedFilepath: String = ""
        val targetfile = File(filepath)

        if (!targetfile.exists())
            return decryptedFilepath

        val targetpath = FileManager.getFolderInCacheFolder(TEMP_FILE_SHARE_FOLDER_NAME, true)
        if (targetfile.length() > 0 && targetpath != null) {
            decryptedFilepath = FileManager.decryptFile(targetfile, pwd, targetpath.canonicalPath, false)
        }

        return decryptedFilepath

    }

    private fun toEnableOkButton(encryptingPassword: String) {
        val okButton = findViewById<Button>(R.id.ok)
        okButton.isEnabled = isDecryptingPasswordValid(encryptingPassword)
    }

    private fun isDecryptingPasswordValid(encryptingPassword: String) : Boolean {
        return (encryptingPassword.length >= MIN_PASSWORD_LENGTH)
    }

}