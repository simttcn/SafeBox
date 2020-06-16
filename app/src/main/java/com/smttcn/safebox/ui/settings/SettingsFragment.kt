package com.smttcn.safebox.ui.settings

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.InputType
import androidx.preference.*
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.WhichButton
import com.afollestad.materialdialogs.actions.setActionButtonEnabled
import com.afollestad.materialdialogs.input.getInputField
import com.afollestad.materialdialogs.input.input
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
            redirectToNewPasswordActivity()

        } else {
            changeAppPassword!!.isEnabled = false
            // ask for current app password before removing the app password
            MaterialDialog(myContext).show {
                input(
                    hintRes = R.string.enter_existing_app_password,
                    inputType = InputType.TYPE_TEXT_VARIATION_PASSWORD
                ) { _, text ->

                    Authenticator().removeAppPassword(text.toString()) {
                        if (it != true) {
                            enabledAppPassword!!.isChecked = true
                            changeAppPassword!!.isEnabled = true

                            MaterialDialog(myContext).show {
                                title(R.string.enter_app_password)
                                message(R.string.enter_app_password_error)
                                positiveButton(R.string.btn_ok)
                            }
                        }
                    }
                }
                positiveButton(R.string.btn_ok)
                negativeButton(R.string.btn_cancel){
                    enabledAppPassword!!.isChecked = true
                    changeAppPassword!!.isEnabled = true
                }
                cancelable(false)  // calls setCancelable on the underlying dialog
                cancelOnTouchOutside(false)  // calls setCanceledOnTouchOutside on the underlying dialog
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

                changeAppPassword!!.isEnabled = true

                // The user set a password, so automatically log user it
                MyApplication.authenticated = true
                MaterialDialog(myContext).show {
                    title(R.string.dlg_title_new_app_password)
                    message(R.string.dlg_msg_new_app_password_success)
                    positiveButton(R.string.btn_ok)
                    cancelable(false)  // calls setCancelable on the underlying dialog
                    cancelOnTouchOutside(false)  // calls setCanceledOnTouchOutside on the underlying dialog
                }

            } else if (resultCode == Activity.RESULT_CANCELED){

                // User cancelled password creation
                enabledAppPassword!!.isChecked = false
                changeAppPassword!!.isEnabled = false

                // User cancelled password change
                MaterialDialog(myContext).show {
                    title(R.string.dlg_title_new_app_password)
                    message(R.string.dlg_msg_new_app_password_cancel)
                    positiveButton(R.string.btn_ok)
                    cancelable(false)  // calls setCancelable on the underlying dialog
                    cancelOnTouchOutside(false)  // calls setCanceledOnTouchOutside on the underlying dialog
                }
            }

        }
    }

}
