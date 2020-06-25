package com.smttcn.safebox.ui.main

import android.app.Activity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.Button
import android.widget.EditText
import android.widget.RadioButton
import android.widget.RadioGroup
import com.smttcn.commons.activities.BaseActivity
import com.smttcn.commons.extensions.isPasswordConfinedToPolicy
import com.smttcn.commons.extensions.showKeyboard
import com.smttcn.commons.extensions.toast
import com.smttcn.commons.helpers.INTENT_RESULT_DECRYPTED
import com.smttcn.commons.helpers.INTENT_RESULT_FAILED
import com.smttcn.commons.helpers.INTENT_RESULT_IMPORTED
import com.smttcn.safebox.R

class ImportingActivity: BaseActivity() {

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
                    btnOk.isEnabled = true
                }
                R.id.importOptionDecryptAndOpenIn -> {
                    password.isEnabled = true
                    password.text.clear()
                    showKeyboard(password)
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

            if (optionSave.isChecked) {

                setResult(INTENT_RESULT_IMPORTED)

            } else if (optionDecryptAndOpen.isChecked) {

                setResult(INTENT_RESULT_DECRYPTED)

            } else {

                setResult(INTENT_RESULT_FAILED)

            }

            finish()

        }
    }

}