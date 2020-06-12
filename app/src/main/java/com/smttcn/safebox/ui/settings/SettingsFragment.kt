package com.smttcn.safebox.ui.settings

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.preference.*
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.callbacks.onDismiss
import com.smttcn.commons.helpers.*
import com.smttcn.commons.helpers.Authenticator
import com.smttcn.safebox.MyApplication
import com.smttcn.safebox.ui.security.PasswordActivity
import com.smttcn.safebox.R
import com.smttcn.safebox.ui.security.LoginActivity

class SettingsFragment : PreferenceFragmentCompat() {

    lateinit var myContext: Context
    var enabledAppPassword: SwitchPreferenceCompat? = null
    var changeAppPassword: Preference? = null

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        val prefManager: PreferenceManager = preferenceManager
        prefManager.sharedPreferencesName = PREFS_KEY
        setPreferencesFromResource(R.xml.root_preferences, rootKey)

        enabledAppPassword = findPreference("enableapppassword")
        changeAppPassword = findPreference("changeapppassword")

        initializeUI()
    }

    private fun initializeUI() {
        if (enabledAppPassword!!.isChecked){
            changeAppPassword!!.isEnabled = true
        } else {
            changeAppPassword!!.isEnabled = false
        }
    }

    override fun onAttach(context: Context) {
        myContext = context
        super.onAttach(context)
    }

    override fun onPreferenceTreeClick(preference: Preference?): Boolean {
        when (preference?.key?.toLowerCase()) {
            "enableapppassword" -> toggleEnableAppPasswordActivity(preference as SwitchPreferenceCompat?)
            "changeapppassword" -> showChangeAppPasswordActivity()
        }

        return super.onPreferenceTreeClick(preference)
    }

    private fun toggleEnableAppPasswordActivity(preference: SwitchPreferenceCompat?) {
        if (preference!!.isChecked){
            changeAppPassword!!.isEnabled = true
            //todo: ask user for a new app password
            redirectToNewPasswordActivity()

        } else {
            changeAppPassword!!.isEnabled = false
            //todo: ask for current app password before removing the app password
            Authenticator().removeAppPassword("111111") {

            }
        }
    }

    private fun redirectToNewPasswordActivity() {
        val intent = Intent(myContext, PasswordActivity::class.java)
        intent.putExtra(INTENT_TO_CREATE_APP_PASSWORD, "yes")
        startActivityForResult(intent, REQUEST_CODE_NEW_APP_PASSWORD)
    }

    private fun showChangeAppPasswordActivity() {
        val intent = Intent(myContext, PasswordActivity::class.java)
        startActivityForResult(intent, REQUEST_CODE_CHANGE_APP_PASSWORD)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        // Check which request we're responding to
        if (requestCode == REQUEST_CODE_CHANGE_APP_PASSWORD) {
            // Make sure the request was successful
            if (resultCode == Activity.RESULT_OK) {
                // The user changed his/her app password
                MaterialDialog(myContext).show {
                    title(R.string.dlg_title_change_app_password)
                    message(R.string.dlg_msg_change_app_password_success)
                    positiveButton(R.string.btn_ok)
                    cancelable(false)  // calls setCancelable on the underlying dialog
                    cancelOnTouchOutside(false)  // calls setCanceledOnTouchOutside on the underlying dialog
                }
            } else if (resultCode == Activity.RESULT_CANCELED){
                // User cancelled password change
                MaterialDialog(myContext).show {
                    title(R.string.dlg_title_change_app_password)
                    message(R.string.dlg_msg_change_app_password_cancel)
                    positiveButton(R.string.btn_ok)
                    cancelable(false)  // calls setCancelable on the underlying dialog
                    cancelOnTouchOutside(false)  // calls setCanceledOnTouchOutside on the underlying dialog
                }
            }
        } else if (requestCode == REQUEST_CODE_NEW_APP_PASSWORD) {

            // Make sure the request was successful
            if (resultCode == Activity.RESULT_OK) {

                // The user set a password, so automatically log user it
                MyApplication.globalAppAuthenticated = "yes"
                MaterialDialog(myContext).show {
                    title(R.string.dlg_title_success)
                    message(R.string.dlg_msg_new_app_password_success)
                    positiveButton(R.string.btn_ok)
                    cancelable(false)  // calls setCancelable on the underlying dialog
                    cancelOnTouchOutside(false)  // calls setCanceledOnTouchOutside on the underlying dialog
                }

            } else {

                // password not set, so turn off the app password feature
                enabledAppPassword!!.isChecked = false

            }

        }

    }

}
