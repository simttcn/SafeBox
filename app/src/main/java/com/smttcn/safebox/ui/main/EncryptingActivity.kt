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
import com.smttcn.commons.manager.FileManager
import com.smttcn.commons.activities.BaseActivity
import com.smttcn.commons.extensions.getFilenameFromPath
import com.smttcn.commons.extensions.removeEncryptedExtension
import com.smttcn.commons.extensions.showKeyboard
import com.smttcn.commons.helpers.*
import com.smttcn.safebox.R
import com.smttcn.safebox.databinding.ActivityEncryptingBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class EncryptingActivity: BaseActivity() {

    private lateinit var binding: ActivityEncryptingBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEncryptingBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        initActivityUI()

        showProgressBar(false)
    }

    override fun onStop() {
        super.onStop()
        setResult(Activity.RESULT_CANCELED)
        finish()
    }

    private fun initActivityUI() {
        val encryptingPassword = binding.encryptingPassword
        val confirmPassword = binding.confirmPassword
        val btnOk = binding.ok
        val btnCancel = binding.cancel

        showKeyboard(encryptingPassword)


        encryptingPassword.addTextChangedListener(object: TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                toEnableOkButton(s.toString(), confirmPassword.text.toString())
            }

        })

        confirmPassword.addTextChangedListener(object: TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                toEnableOkButton(encryptingPassword.text.toString(), s.toString())
            }

        })

        btnCancel.setOnClickListener {
            // user cancel
            setResult(Activity.RESULT_CANCELED)
            finish()
        }

        btnOk.setOnClickListener {
            if (isEncryptingPasswordValid(encryptingPassword.text.toString(), confirmPassword.text.toString())){

                showProgressBar(true)

                GlobalScope.launch(Dispatchers.IO) {

                    var encryptedSuccess = false

                    var fileUri = intent.getParcelableExtra<Parcelable>(INTENT_SHARE_FILE_URI) as Uri
                    var encryptedFilePath = FileManager.encryptFileFromUriToFolder(contentResolver, encryptingPassword.text.toString().toCharArray(), fileUri)

                    if (FileManager.isFileExist(encryptedFilePath))
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

            binding.encryptingActivityProgressBarContainer.visibility = View.VISIBLE
        } else {
            window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)

            binding.encryptingActivityProgressBarContainer.visibility = View.GONE
        }
    }



}