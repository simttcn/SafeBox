package com.smttcn.safebox.ui.settings

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.InputType
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.PreferenceManager
import androidx.preference.SwitchPreferenceCompat
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.input.input
import com.smttcn.commons.extensions.showMessageDialog
import com.smttcn.commons.helpers.*
import com.smttcn.safebox.BuildConfig
import com.smttcn.safebox.MyApplication
import com.smttcn.safebox.R
import com.smttcn.safebox.ui.security.PasswordActivity


// todo next: finish up Settings page
class SettingsFragment : PreferenceFragmentCompat() {

    lateinit var myContext: Context
    var enabledAppPassword: SwitchPreferenceCompat? = null
    var changeAppPassword: Preference? = null


    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        val prefManager: PreferenceManager = preferenceManager
        prefManager.sharedPreferencesName = PREFS_KEY
        setPreferencesFromResource(R.xml.root_preferences, rootKey)

        var appInfo: Preference? = findPreference("appinfo")
        var buildText = if (BuildConfig.DEBUG) " Debug" else ""
        var versionText = " " + BuildConfig.VERSION_NAME + "." + BuildConfig.VERSION_CODE
        appInfo?.summary = getString(R.string.pref_app_build_no) + versionText + buildText + "\n" + getString(R.string.pref_app_copyright)

        enabledAppPassword = findPreference("enableapppassword")
        changeAppPassword = findPreference("changeapppassword")

        enabledAppPassword?.setOnPreferenceClickListener {
            toggleEnableAppPasswordActivity(it as SwitchPreferenceCompat?)
            true
        }

        changeAppPassword?.setOnPreferenceClickListener {
            showChangeAppPasswordActivity()
            true
        }


        initializeUI()
    }


    private fun initializeUI() {
        changeAppPassword!!.isEnabled = enabledAppPassword!!.isChecked
    }


    override fun onAttach(context: Context) {
        myContext = context
        super.onAttach(context)
    }


    private fun toggleEnableAppPasswordActivity(preference: SwitchPreferenceCompat?) {
        if (preference!!.isChecked){
            redirectToNewPasswordActivity()

        } else {
            // ask for current app password before removing the app password
            MaterialDialog(myContext).show {
                title(R.string.enter_app_password)
                input(
                    inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
                ) { _, text ->

                    Authenticator().removeAppPassword(text.toString()) {
                        if (it == true) {
                            enabledAppPassword!!.isChecked = false
                            changeAppPassword!!.isEnabled = false
                        } else {
                            enabledAppPassword!!.isChecked = true
                            changeAppPassword!!.isEnabled = true

                            showMessageDialog(
                                myContext,
                                R.drawable.ic_warning,
                                R.string.dlg_title_error,
                                R.string.enter_app_password_error
                            ) {}
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
                showMessageDialog(
                    myContext,
                    R.drawable.ic_info,
                    R.string.dlg_title_success,
                    R.string.dlg_msg_change_app_password_success
                ) {}
            } else if (resultCode == Activity.RESULT_CANCELED){
                // User cancelled password change
                showMessageDialog(
                    myContext,
                    R.drawable.ic_info,
                    R.string.dlg_title_change_app_password,
                    R.string.dlg_msg_change_app_password_cancel
                ) {}
            }
        } else if (requestCode == REQUEST_CODE_NEW_APP_PASSWORD) {

            // Make sure the request was successful
            if (resultCode == Activity.RESULT_OK) {

                changeAppPassword!!.isEnabled = true

                // The user set a password, so automatically log user it
                MyApplication.authenticated = true
                showMessageDialog(
                    myContext,
                    R.drawable.ic_info,
                    R.string.dlg_title_new_app_password,
                    R.string.dlg_msg_new_app_password_success
                ) {}

            } else if (resultCode == Activity.RESULT_CANCELED){

                // User cancelled password creation
                enabledAppPassword!!.isChecked = false
                changeAppPassword!!.isEnabled = false

//                // User cancelled password change
//                showMessageDialog(
//                    myContext,
//                    R.drawable.ic_info,
//                    R.string.dlg_title_new_app_password,
//                    R.string.dlg_msg_new_app_password_cancel
//                ) {}
            }

        }
    }

}
