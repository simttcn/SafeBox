package com.smttcn.safebox.ui.main

import android.app.Activity
import android.content.ContentResolver
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
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.customview.getCustomView
import com.afollestad.materialdialogs.input.input
import com.smttcn.commons.activities.BaseActivity
import com.smttcn.commons.extensions.*
import com.smttcn.commons.helpers.*
import com.smttcn.commons.manager.FileManager
import com.smttcn.commons.models.FileDirItem
import com.smttcn.safebox.R
import kotlinx.android.synthetic.main.activity_importing.*

class ImportingActivity: BaseActivity() {

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

        optionGroup.setOnCheckedChangeListener { group, checkedId ->
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

        password.addTextChangedListener(object: TextWatcher {
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

                var filename = FileManager.copyFileFromUriToFolder(contentResolver, fileUri)

                var resultIntent = Intent()
                resultIntent.putExtra(INTENT_IMPORTED_FILENAME, filename.getFilenameFromPath().removeEncryptedExtension())
                setResult(INTENT_RESULT_IMPORTED, resultIntent)

            } else if (optionDecryptAndOpen.isChecked) {

                //todo next: decrypt and share
                //shareItemDecrypted(contentResolver, fileUri)
                setResult(INTENT_RESULT_DECRYPTED)

            } else {

                setResult(INTENT_RESULT_FAILED)

            }

            finish()

        }
    }


//    private fun shareItemDecrypted(contentResolver: ContentResolver, uri: Uri) {
//        // file: FileDirItem
//
//        // ask for the decrypting password for this file
//        MaterialDialog(this).show {
//            title(R.string.enc_enter_password)
//            message(R.string.enc_msg_decrypting_password)
//            input(
//                inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
//            ) { _, text ->
//
//                showProgressBar(true)
//                var decryptedFilePath =
//                    decryptFileForSharing(text.toString().toCharArray(), file.path)
//                showProgressBar(false)
//
//                if (FileManager.isFileExist(decryptedFilePath)) {
//                    // succesfully decrypted file
//                    sendShareItent((decryptedFilePath))
//                } else {
//                    // fail to encrypt file
//                    showMessageDialog(
//                        myContext,
//                        R.string.error,
//                        R.string.enc_enter_decrypting_password_error
//                    ) {}
//                }
//            }
//            positiveButton(R.string.btn_decrypt_file)
//            negativeButton(R.string.btn_cancel)
//            cancelable(false)  // calls setCancelable on the underlying dialog
//            cancelOnTouchOutside(false)  // calls setCanceledOnTouchOutside on the underlying dialog
//        }
//    }

    private fun showProgressBar(show: Boolean) {
        if (show) {
            getWindow().setFlags(
                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
            );

            importingActivityProgressBarContainer.visibility = View.VISIBLE
            //itemListRecyclerView.visibility = View.GONE
        } else {
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);

            importingActivityProgressBarContainer.visibility = View.GONE
            //itemListRecyclerView.visibility = View.VISIBLE
        }
    }



}