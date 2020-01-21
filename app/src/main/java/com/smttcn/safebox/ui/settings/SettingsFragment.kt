package com.smttcn.safebox.ui.settings

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.PreferenceManager
import com.smttcn.commons.helpers.PREFS_KEY
import com.smttcn.commons.helpers.REQUEST_CODE_CHANGE_PASSWORD
import com.smttcn.materialdialogs.MaterialDialog
import com.smttcn.safebox.ui.security.PasswordActivity
import com.smttcn.safebox.R

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
        if (preference?.key.equals("changepassword"))
            showChangePasswordActivity()

        return super.onPreferenceTreeClick(preference)
    }

    private fun showChangePasswordActivity() {
        val intent = Intent(myContext, PasswordActivity::class.java)
        startActivityForResult(intent, REQUEST_CODE_CHANGE_PASSWORD)
    }

    //Todo: to have an activity to let user change the database's secret
    private fun showChangeDatabaseSecretActivity() {
        TODO()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        // Check which request we're responding to
        if (requestCode == REQUEST_CODE_CHANGE_PASSWORD) {
            // Make sure the request was successful
            if (resultCode == Activity.RESULT_OK) {
                // The user changed his/her app password
                MaterialDialog(myContext).show {
                    title(R.string.change_password)
                    message(R.string.change_password_message)
                    positiveButton(R.string.ok)
                    cancelable(false)  // calls setCancelable on the underlying dialog
                    cancelOnTouchOutside(false)  // calls setCanceledOnTouchOutside on the underlying dialog
                }
            } else if (resultCode == Activity.RESULT_CANCELED){
                // User cancelled password change
                MaterialDialog(myContext).show {
                    title(R.string.change_password)
                    message(R.string.change_password_cancel_message)
                    positiveButton(R.string.ok)
                    cancelable(false)  // calls setCancelable on the underlying dialog
                    cancelOnTouchOutside(false)  // calls setCanceledOnTouchOutside on the underlying dialog
                }
            }
        }
    }

}
