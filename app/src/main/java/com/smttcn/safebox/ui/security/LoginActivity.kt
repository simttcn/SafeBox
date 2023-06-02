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
import com.smttcn.commons.extensions.showKeyboard
import com.smttcn.commons.helpers.Authenticator
import com.smttcn.commons.helpers.MIN_PASSWORD_LENGTH
import com.smttcn.safebox.MyApplication
import com.smttcn.safebox.R
import com.smttcn.safebox.databinding.ActivityLoginBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch


class LoginActivity : BaseActivity() {

    private lateinit var binding: ActivityLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)

        authenticationExempted = true

        setContentView(binding.root)
        initializeUI()

        showProgressBar(false)
    }


    override fun onResume() {
        super.onResume()
        MyApplication.lockApp()
    }

    private fun initializeUI() {
        val password = findViewById<EditText>(R.id.password)
        val loginButton = findViewById<Button>(R.id.login)

        password.text.clear()

        showKeyboard(password)

        password.addTextChangedListener(object: TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                loginButton.isEnabled = s.toString().length >= MIN_PASSWORD_LENGTH
            }

        })

        loginButton.setOnClickListener {
            // perform authenticaiton here
            val authenticator: Authenticator = Authenticator()

            if (password.length() >= MIN_PASSWORD_LENGTH) {
                authenticator.authenticateAppPassword((password.text.toString())) {
                    //showProgressBar(false)
                    password.text.clear()
                    if (it) {
                        // Login successfully
                        MyApplication.authenticated = true
                        // finish and return
                        finish()
                    } else {
                        MyApplication.lockApp()
                        val message = findViewById<TextView>(R.id.message)
                        message.text = getString(R.string.enter_app_password_error)
                        message.visibility = View.VISIBLE
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

                binding.loginActivityProgressBarContainer.visibility = View.VISIBLE
            } else {
                window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)

                binding.loginActivityProgressBarContainer.visibility = View.GONE
            }

        }

    }




}
