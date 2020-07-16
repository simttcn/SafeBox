package com.smttcn.safebox.ui.security

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import com.smttcn.commons.activities.BaseActivity
import com.smttcn.commons.helpers.Authenticator
import com.smttcn.commons.helpers.MIN_PASSWORD_LENGTH
import com.smttcn.safebox.MyApplication
import com.smttcn.safebox.R
import kotlinx.android.synthetic.main.activity_login.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch


class LoginActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        authenticationExempted = true

        setContentView(R.layout.activity_login)
        initializeUI()

        showProgressBar(false)
    }


    override fun onResume() {
        super.onResume()
        MyApplication.lockApp()
    }

    private fun initializeUI() {
        val Password = findViewById<EditText>(R.id.password)
        val LoginButton = findViewById<Button>(R.id.login)

        Password.text.clear()

        Password.addTextChangedListener(object: TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                LoginButton.isEnabled = s.toString().length >= MIN_PASSWORD_LENGTH
            }

        })

        LoginButton.setOnClickListener {
            // perform authenticaiton here
            val authenticator: Authenticator = Authenticator()

            if (Password.length() >= MIN_PASSWORD_LENGTH) {
                authenticator.authenticateAppPassword((Password.text.toString())) {
                    //showProgressBar(false)
                    Password.text.clear()
                    if (it == true) {
                        // Login successfully
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


    private fun showProgressBar(show: Boolean) {

        GlobalScope.launch(Dispatchers.Main) {

            if (show) {
                window.setFlags(
                    WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                    WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
                )

                loginActivityProgressBarContainer.visibility = View.VISIBLE
            } else {
                window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)

                loginActivityProgressBarContainer.visibility = View.GONE
            }

        }

    }




}
