package com.smttcn.safebox.ui.settings

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.widget.Toast
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.PreferenceManager
import com.smttcn.commons.crypto.KeyUtil
import com.smttcn.commons.extensions.toast
import com.smttcn.commons.helpers.Authenticator
import com.smttcn.commons.helpers.MIN_PASSWORD_LENGTH
import com.smttcn.commons.helpers.PREFS_KEY
import com.smttcn.commons.helpers.REQUEST_CODE_CHANGE_PASSWORD
import com.smttcn.materialdialogs.MaterialDialog
import com.smttcn.materialdialogs.WhichButton
import com.smttcn.materialdialogs.actions.setActionButtonEnabled
import com.smttcn.materialdialogs.input.getInputField
import com.smttcn.materialdialogs.input.input
import com.smttcn.materialdialogs.lifecycle.lifecycleOwner
import com.smttcn.safebox.ui.security.PasswordActivity
import com.smttcn.safebox.R
import com.smttcn.safebox.database.AppDatabase

class SettingsFragment : PreferenceFragmentCompat() {

    lateinit var myContext: Context

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        val prefManager: PreferenceManager = preferenceManager
        prefManager.sharedPreferencesName = PREFS_KEY
        setPreferencesFromResource(R.xml.root_preferences, rootKey)
    }

    override fun onAttach(context: Context) {
        myContext = context
        super.onAttach(context)
    }

    override fun onPreferenceTreeClick(preference: Preference?): Boolean {
        when (preference?.key?.toLowerCase()) {
            "changepassword" -> showChangePasswordActivity()
            "resetdatakey" -> showResetDataKeyActivity()
        }

        return super.onPreferenceTreeClick(preference)
    }

    private fun showChangePasswordActivity() {
        val intent = Intent(myContext, PasswordActivity::class.java)
        startActivityForResult(intent, REQUEST_CODE_CHANGE_PASSWORD)
    }

    //Todo: to have an activity to let user change the database's secret
    private fun showResetDataKeyActivity() {
        val ku = KeyUtil()

            MaterialDialog(myContext).show {
                title(R.string.dlg_title_reset_data_key)
                message(R.string.dlg_msg_reset_data_key_confirmation)
                input(
                    hint = "Enter password to proceed",
                    inputType = InputType.TYPE_TEXT_VARIATION_PASSWORD,
                    waitForPositiveButton = false
                ) { _, text ->
                    setActionButtonEnabled(WhichButton.POSITIVE, text.length >= MIN_PASSWORD_LENGTH)
                }
                positiveButton(R.string.btn_ok) {
                    // Todo: find a cleaner way to  perform reset data key operation
                    // Todo: app crashed when trying to read again after key reset
                    val pwd = it.getInputField().text.toString()
                    Authenticator().authenticateAppPassword(pwd) {
                        if (it == true) {
                            val newSecret = KeyUtil().generateAndSaveAppDatabaseSecret(pwd,true)
                            if (!newSecret.isEmpty()) {
                                AppDatabase.reKey(newSecret)
                                myContext.toast("Key successfully reset.")
                            } else
                                myContext.toast("Key reset failed!")
                        } else {
                            myContext.toast("Incorrect password!")
                        }
                    }
                }
                negativeButton(R.string.btn_cancel)
                lifecycleOwner(this@SettingsFragment)
            }

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        // Check which request we're responding to
        if (requestCode == REQUEST_CODE_CHANGE_PASSWORD) {
            // Make sure the request was successful
            if (resultCode == Activity.RESULT_OK) {
                // The user changed his/her app password
                MaterialDialog(myContext).show {
                    title(R.string.dlg_title_change_password)
                    message(R.string.dlg_msg_change_password_success)
                    positiveButton(R.string.btn_ok)
                    cancelable(false)  // calls setCancelable on the underlying dialog
                    cancelOnTouchOutside(false)  // calls setCanceledOnTouchOutside on the underlying dialog
                }
            } else if (resultCode == Activity.RESULT_CANCELED){
                // User cancelled password change
                MaterialDialog(myContext).show {
                    title(R.string.dlg_title_change_password)
                    message(R.string.dlg_msg_change_password_cancel)
                    positiveButton(R.string.btn_ok)
                    cancelable(false)  // calls setCancelable on the underlying dialog
                    cancelOnTouchOutside(false)  // calls setCanceledOnTouchOutside on the underlying dialog
                }
            }
        }
    }

}
