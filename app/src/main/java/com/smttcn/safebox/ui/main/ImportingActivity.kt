package com.smttcn.safebox.ui.main

import android.app.Activity
import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Parcelable
import android.text.Editable
import android.text.InputType
import android.text.TextWatcher
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.EditText
import android.widget.RadioButton
import android.widget.RadioGroup
import androidx.core.content.FileProvider
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.input.getInputField
import com.afollestad.materialdialogs.input.input
import com.smttcn.commons.activities.BaseActivity
import com.smttcn.commons.extensions.*
import com.smttcn.commons.helpers.*
import com.smttcn.commons.manager.FileManager
import com.smttcn.safebox.MyApplication
import com.smttcn.safebox.R
import kotlinx.android.synthetic.main.activity_importing.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileInputStream
import java.io.ObjectInputStream
import java.lang.Exception

class ImportingActivity : BaseActivity() {

    lateinit var myContext: Context

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
        myContext = this
        setContentView(R.layout.activity_importing)
    }


    private fun initActivityUI() {
        val optionGroup = findViewById<RadioGroup>(R.id.importOptionGroup)
        val optionSave = findViewById<RadioButton>(R.id.importOptionSaveInLibrary)
        val optionDecryptAndOpen = findViewById<RadioButton>(R.id.importOptionDecryptAndOpenIn)
        val password = findViewById<EditText>(R.id.password)
        val btnOk = findViewById<Button>(R.id.ok)
        val btnCancel = findViewById<Button>(R.id.cancel)


        optionSave.isChecked = true
        password.isEnabled = false
        btnOk.isEnabled = true

        optionGroup.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                R.id.importOptionSaveInLibrary -> {
                    password.text.clear()
                    password.isEnabled = false
                    btnOk.text = getString(R.string.btn_save)
                    btnOk.isEnabled = true
                }
                R.id.importOptionDecryptAndOpenIn -> {
                    password.isEnabled = true
                    password.text.clear()
                    showKeyboard(password)
                    btnOk.text = getString(R.string.btn_decrypt)
                    btnOk.isEnabled = false
                }
                else -> {
                }
            }
        }

        password.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                btnOk.isEnabled = isPasswordConfinedToPolicy(password.text.toString())
            }

        })
        btnCancel.setOnClickListener {
            // user cancel
            setResult(Activity.RESULT_CANCELED)
            finish()
        }

        btnOk.setOnClickListener {

            var fileUri = intent.getParcelableExtra<Parcelable>(INTENT_SHARE_FILE_URI) as Uri

            if (optionSave.isChecked) {

                showProgressBar(true)

                GlobalScope.launch(Dispatchers.IO) {

                    var filename = FileManager.copyFileFromUriToFolder(contentResolver, fileUri)

                    var resultIntent = Intent()
                    resultIntent.putExtra(INTENT_IMPORTED_FILENAME, filename.getFilenameFromPath().removeEncryptedExtension())
                    setResult(INTENT_RESULT_IMPORTED, resultIntent)

                    launch(Dispatchers.Main) {

                        showProgressBar(false)
                        finish()

                    }
                }

            } else if (optionDecryptAndOpen.isChecked) {

                // decrypt and share
                showProgressBar(true)

                var decryptedFilePath: String = ""

                GlobalScope.launch(Dispatchers.IO) {

                    val inputStream = contentResolver.openInputStream(fileUri)
                    val (filename, _) = FileManager.getFilenameAndSizeFromUri(contentResolver, fileUri)
                    try {
                        if(inputStream != null) {
                            decryptedFilePath = FileManager.decryptInputStreamForSharing(inputStream, filename, password.text.toString().toCharArray())
                        }
                    } catch (ex: Exception) {
                    }

                    launch(Dispatchers.Main) {
                        showProgressBar(false)
                        if (FileManager.isFileExist(decryptedFilePath)) {
                            // succesfully decrypted file
                            sendShareItent(myContext, decryptedFilePath)
                            setResult(INTENT_RESULT_DECRYPTED)
                            finish()
                        } else {
                            // fail to encrypt file
                            showMessageDialog(
                                myContext,
                                R.string.error,
                                R.string.enc_enter_decrypting_password_error
                            ) {
                                setResult(INTENT_RESULT_FAILED)
                                finish()
                            }
                        }
                    }
                }

            } else {

                setResult(INTENT_RESULT_FAILED)
                finish()

            }
        }
    }


    private fun showProgressBar(show: Boolean) {
        if (show) {
            window.setFlags(
                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
            )

            importingActivityProgressBarContainer.visibility = View.VISIBLE
            //itemListRecyclerView.visibility = View.GONE
        } else {
            window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)

            importingActivityProgressBarContainer.visibility = View.GONE
            //itemListRecyclerView.visibility = View.VISIBLE
        }
    }


}