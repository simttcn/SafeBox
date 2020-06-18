package com.smttcn.safebox.ui.security

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import com.smttcn.commons.activities.BaseActivity
import com.smttcn.commons.helpers.Authenticator
import com.smttcn.commons.helpers.MIN_PASSWORD_LENGTH
import com.smttcn.safebox.MyApplication
import com.smttcn.safebox.R


class LoginActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        authenticationExempted = true

        setContentView(R.layout.activity_login)
        initializeUI()
    }


    override fun onResume() {
        super.onResume()
        MyApplication.lockApp()
    }

    private fun initializeUI() {
        val Password = findViewById<EditText>(R.id.password)
        val LoginButton = findViewById<Button>(R.id.login)

        Password.addTextChangedListener(object: TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                LoginButton.isEnabled = s.toString().length >= MIN_PASSWORD_LENGTH
            }

        })

        LoginButton.setOnClickListener {
            MyApplication.cleanUp()
            // perform authenticaiton here
            val authenticator: Authenticator = Authenticator()
            if (Password.length() >= MIN_PASSWORD_LENGTH) {
                authenticator.authenticateAppPassword((Password.text.toString())) {
                    if (it == true) {
                        // Login successfully
                        Password.text.clear()
                        MyApplication.authenticated = true
                        // finish and return
                        finish()
                    } else {
                        MyApplication.lockApp()
                        val Message = findViewById<TextView>(R.id.message)
                        Message.text = getString(R.string.enter_app_password_error)
                        Message.visibility = View.VISIBLE
                    }
                }
            }
        }
    }

}
