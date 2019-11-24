package com.smttcn.safebox

import android.app.Activity
import android.app.Application
import android.content.*
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import android.os.Bundle
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.Toast
import android.view.inputmethod.InputMethodManager.SHOW_IMPLICIT
import android.content.Context.INPUT_METHOD_SERVICE
import androidx.core.content.ContextCompat.getSystemService
import androidx.core.app.ComponentActivity.ExtraData
import android.icu.lang.UCharacter.GraphemeClusterBreak.T
import android.view.inputmethod.InputMethodManager
import com.smttcn.commons.helpers.Authenticator
import com.smttcn.commons.helpers.BaseConfig
import com.smttcn.commons.helpers.REQUEST_CODE_NEW_PASSWORD


class LoginActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val authenticator = Authenticator(this)
        if (!authenticator.isAppPasswordHashExist()) redirectToNewPasswordActivity()

        setContentView(R.layout.activity_login)

        val Password = findViewById<EditText>(R.id.password)
        val LoginButton = findViewById<Button>(R.id.login)

        Password.addTextChangedListener(object: TextWatcher {

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                LoginButton.isEnabled = s.toString().length > 5
            }

        })

        LoginButton.setOnClickListener {
            //Loading.visibility = View.VISIBLE

            // togo: perform authenticaiton here
            //loginViewModel.login(username.text.toString(), password.text.toString())
        }
    }

    private fun redirectToNewPasswordActivity() {
        val intent = Intent(this, PasswordActivity::class.java)
        startActivityForResult(intent, REQUEST_CODE_NEW_PASSWORD)
    }

    private fun initialize() {

        val baseConfig: BaseConfig = BaseConfig.newInstance(this)

    }

    private fun showLoginFailed(@StringRes errorString: Int) {
        Toast.makeText(applicationContext, errorString, Toast.LENGTH_SHORT).show()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        // Check which request we're responding to
        if (requestCode == REQUEST_CODE_NEW_PASSWORD) {
            // Make sure the request was successful
            if (resultCode == Activity.RESULT_OK) {
                // The user set a password, so will return to this activity for user to login
            } else {
                // password not set, so quit the app
                finishAndRemoveTask()
            }
        }
    }
}
