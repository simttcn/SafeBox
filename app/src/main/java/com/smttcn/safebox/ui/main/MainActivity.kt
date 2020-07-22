package com.smttcn.safebox.ui.main

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.os.Parcelable
import android.text.Editable
import android.text.InputType
import android.text.SpannableString
import android.text.TextWatcher
import android.text.style.ForegroundColorSpan
import android.view.*
import android.widget.EditText
import android.widget.PopupMenu
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.activity.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.WhichButton
import com.afollestad.materialdialogs.actions.getActionButton
import com.afollestad.materialdialogs.customview.customView
import com.afollestad.materialdialogs.customview.getCustomView
import com.afollestad.materialdialogs.input.input
import com.afollestad.materialdialogs.lifecycle.lifecycleOwner
import com.google.android.material.snackbar.Snackbar
import com.smttcn.commons.activities.BaseActivity
import com.smttcn.commons.extensions.*
import com.smttcn.commons.helpers.*
import com.smttcn.commons.manager.FileManager
import com.smttcn.commons.models.FileDirItem
import com.smttcn.safebox.MyApplication
import com.smttcn.safebox.R
import com.smttcn.safebox.managers.ViewerManager
import com.smttcn.safebox.ui.debug.DebugconsoleActivity
import com.smttcn.safebox.ui.settings.SettingsActivity
import com.smttcn.safebox.viewmodel.FileItemViewModel
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.util.*
import kotlin.concurrent.schedule
import kotlin.coroutines.CoroutineContext


class MainActivity : BaseActivity() {

    private val FILE_PASSWORD_SEPERATOR = "=::::="
    private val fileItemViewModel: FileItemViewModel by viewModels()
    private lateinit var recyclerView: RecyclerView
    private lateinit var recyclerViewAdapter: FileItemAdapter
    private var IsShareFromOtherApp = false
    private lateinit var toolbarMenu: Menu
    private lateinit var myContext: Context

    var BackButtonPressedOnce = false
    var LastPressedBackTime = System.currentTimeMillis()


    // todo: Investigate "The application may be doing too much work on its main thread"
    override fun onCreate(savedInstanceState: Bundle?) {
        MyApplication.mainActivityContext = this
        myContext = this
        IsShareFromOtherApp = intent?.action == Intent.ACTION_SEND

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

//        fab.setOnClickListener { view ->
//            Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
//                .setAction("Action", null).show()
//        }

        if (IsShareFromOtherApp) {
            IsShareFromOtherApp = false
            handleShareFromOtherApp()
        }

        initializeUI()

    }


    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)

        IsShareFromOtherApp = intent?.action == Intent.ACTION_SEND

        if (IsShareFromOtherApp) {
            IsShareFromOtherApp = false
            handleShareFromOtherApp()
        }
    }


    private fun initializeUI() {

        showProgressBar(true)

        recyclerView = findViewById<RecyclerView>(R.id.itemListRecyclerView)
        recyclerViewAdapter = FileItemAdapter(myContext)
        recyclerView.adapter = recyclerViewAdapter
        recyclerView.layoutManager = LinearLayoutManager(myContext)

        GlobalScope.launch(Dispatchers.IO) {

            recyclerViewAdapter.onItemClick = { view, item, prevIdx, currIdx ->
                onRecyclerViewItemClicked(view, item, prevIdx, currIdx)
            }

            recyclerViewAdapter.onItemPopupMenuClick = { view, item, position ->
                onRecyclerViewItemPopupMenuClicked(view, item, position)
            }

            launch(Dispatchers.Main) {
                fileItemViewModel.allFileItems.observe(this@MainActivity, Observer { item ->
                    // Update the cached copy of the fileItems in the adapter.
                    item?.let { recyclerViewAdapter.setFileItems(it as MutableList<FileDirItem>) }
                })
                showProgressBar(false)
            }
        }
    }


    private fun handleShareFromOtherApp(): Boolean {
        var result: Boolean = true

        val uri = intent.getParcelableExtra<Parcelable>(Intent.EXTRA_STREAM) as? Uri

        if (uri != null) {
            val (filename, _) = FileManager.getFilenameAndSizeFromUri(contentResolver, uri)

            toast(filename)

            if (filename.getFileExtension() == ENCRYPTED_FILE_EXT) {
                // check if the shared file is a supported encrypted file
                if (FileManager.isEncryptedFileUri(contentResolver, uri)) {

                    // It is an encrypted file, so ask user if they want to save it in the library or decrypt it
                    val importIntent = Intent(this, ImportingActivity::class.java)
                    importIntent.putExtra(INTENT_SHARE_FILE_URI, uri)
                    startActivityForResult(importIntent, REQUEST_CODE_TO_IMPORTDECRYPT_FILE)

                } else {

                    showMessageDialog(this, R.string.error, R.string.imp_msg_invalid_file){
                        finishAndRemoveTask()
                    }

                }

            } else {
                // it's an ordinary file, show encrypting option and password input
                val encryptingIntent = Intent(this, EncryptingActivity::class.java)
                encryptingIntent.putExtra(INTENT_SHARE_FILE_URI, uri)
                startActivityForResult(encryptingIntent, REQUEST_CODE_TO_ENCRYPT_FILE)
            }

        }

        return result
    }


    override fun onBackPressed() {
        val millisecondsSinceLastPressedBack = System.currentTimeMillis() - LastPressedBackTime
        if (!BackButtonPressedOnce || millisecondsSinceLastPressedBack > INTERVAL_BACK_BUTTON_QUIT_IN_MS) {
            toast(R.string.press_back_again_to_quit, Toast.LENGTH_LONG)
            BackButtonPressedOnce = true
            LastPressedBackTime = System.currentTimeMillis()
            return
        } else {
            //showProgressBar(true)
            finishAndRemoveTask()
        }
        super.onBackPressed()
    }

    override fun onDestroy() {
        super.onDestroy()
    }


    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        toolbarMenu = menu

        return super.onCreateOptionsMenu(menu)
    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        when (item.itemId) {
            R.id.menu_visible -> {
                GlobalScope.launch(Dispatchers.Main) {
                    showProgressBar(true)
                    Timer().schedule(5000) {
                        GlobalScope.launch(Dispatchers.Main) {
                            showProgressBar(false)
                        }
                    }
                }
                return true
            }
            R.id.menu_refresh -> {
                refreshFileItemList()
                return true
            }
            R.id.menu_settings -> {
                startActivity(Intent(this@MainActivity, SettingsActivity::class.java))
                return true
            }
            R.id.menu_debug_console -> {
                startActivity(Intent(this@MainActivity, DebugconsoleActivity::class.java))
                return true
            }
        }

        return super.onOptionsItemSelected(item)
    }


    @Suppress("UNUSED_PARAMETER")
    private fun onRecyclerViewItemClicked(view: View, item: FileDirItem, prevIdx: Int, currIdx: Int) {
        // Todo: to decide whether should allow user to open just by taping on it.

//        // set previous selected view background to normal
//        val previousItemView = recyclerView.findViewHolderForAdapterPosition(prevIdx)
//        if (previousItemView != null)
//            previousItemView.itemView.setBackgroundColor(ContextCompat.getColor(this, R.color.colorItemBackgroundNormal))
//
//        // highlight the current selected view
//        view.setBackgroundColor(ContextCompat.getColor(this, R.color.colorPrimaryLight))


        //toast(item.filename + " clicked")

        // update toolbar icon status
        if (recyclerViewAdapter.getSelectedItemCount() > 0) {
            // item selected
        } else {
            // no item selected
        }

//        val targetfile = item.path
//        if (targetfile.length > 0) {
//            val imagePaths = listOf(targetfile)
//            StfalconImageViewer.Builder<String>(this, imagePaths, ::loadImage)
//                .withTransitionFrom(view.item_background)
//                .show()
//        }
    }


    private fun onRecyclerViewItemPopupMenuClicked(view: View, item: FileDirItem, position: Int) {
        var popupMenu = PopupMenu(this, view, Gravity.BOTTOM + Gravity.END)
        popupMenu.menuInflater.inflate(R.menu.filediritem_popup_menu, popupMenu.menu)

        // color the last menu item "Delete" as red colour
        val menuItem: MenuItem = popupMenu.menu.getItem(popupMenu.menu.size() - 1)
        val menuItemText = menuItem.title
        val s = SpannableString(menuItemText)
        s.setSpan(ForegroundColorSpan(Color.RED), 0, s.length, 0)
        menuItem.title = s

        // disable the first menu item "View" if no viewer support for this file
        if (!ViewerManager.hasSupportedViewer(item)) {
            popupMenu.menu.getItem(0).isVisible = false
        }

        popupMenu.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.popupmenu_view -> {
                    viewItem(item, view)
                    true
                }
                R.id.popupmenu_share_encrypt -> {
                    // copy the file to be shared to the designated share folder
                    val shareFilePath = FileManager.copyFileToTempShareFolder(item.path)
                    if (shareFilePath.length > 0) {
                        sendShareItent(myContext, shareFilePath)
                    }
                    true
                }
                R.id.popupmenu_share_decrypt -> {
                    shareItemDecrypted(item)
                    true
                }
                R.id.popupmenu_change_encrypting_password -> {
                    changeEncryptingPassword(item)
                    true
                }
                R.id.popupmenu_delete_item -> {
                    recyclerViewAdapter.deleteFileItem(position)
                    //showMessageDialog(this, R.id.popupmenu_delete_item.toString(), item.filename){}
                    true
                }
                else -> false
            }

        }

        item.isOptionMenuActive = true
        view.setBackgroundColor(ContextCompat.getColor(this, R.color.colorItemBackgroundOptionMenuActive))

        popupMenu.setOnDismissListener {
            item.isOptionMenuActive = false
            view.setBackgroundColor(ContextCompat.getColor(this, R.color.colorItemBackgroundNormal))
        }

        popupMenu.show()

    }


    // let people view the contetnt of supported file
    private fun viewItem(item: FileDirItem, view: View) {

        // get the appropriate viewer through the viewer manager
        val helper = ViewerManager.getHelper(this, view, item)

        if (helper != null) { // helper not null

            promptDecryptingPassword {
                helper.view(it)
            }

        }

    }


    private fun promptDecryptingPassword(callback: (password: CharArray) -> Unit) {

        // ask for the decrypting password for this file
        val dialog = MaterialDialog(this).show {
            title(R.string.enc_enter_password)
            customView(R.layout.enter_password_view, scrollable = true, horizontalPadding = true)
            positiveButton(R.string.btn_ok)
            negativeButton(R.string.btn_cancel)
            cancelable(false)  // calls setCancelable on the underlying dialog
            cancelOnTouchOutside(false)  // calls setCanceledOnTouchOutside on the underlying dialog
        }

        val passwordInput: EditText = dialog.getCustomView().findViewById(R.id.password)
        val progressBarContainer: View = dialog.getCustomView().findViewById(R.id.progressBarContainer)
        var btnOk = dialog.getActionButton(WhichButton.POSITIVE)
        var btnCancel = dialog.getActionButton(WhichButton.NEGATIVE)

        progressBarContainer.visibility = View.GONE
        btnCancel.isEnabled = true
        btnOk.isEnabled = false
        showKeyboard(passwordInput)

        passwordInput.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                btnOk.isEnabled = isPasswordConfinedToPolicy(passwordInput.text.toString())
            }

        })

        btnOk.setOnClickListener {
            // display a progress activity when decrypting file.
            btnOk.isEnabled = false
            btnCancel.isEnabled = false
            progressBarContainer.visibility = View.VISIBLE

            GlobalScope.launch(Dispatchers.IO) {

                // Pull the password out of the custom view when the positive button is pressed
                val password = passwordInput.text.toString()

                callback(password.toCharArray())

                launch(Dispatchers.Main) {

                    dialog.dismiss()

                }

            }

        }

    }


    private fun shareItemDecrypted(file: FileDirItem) {

        // ask for the decrypting password for this file
        val dialog = MaterialDialog(this).show {
            title(R.string.enc_enter_password)
            customView(R.layout.enter_password_view, scrollable = true, horizontalPadding = true)
            positiveButton(R.string.btn_ok)
            negativeButton(R.string.btn_cancel)
            cancelable(false)  // calls setCancelable on the underlying dialog
            cancelOnTouchOutside(false)  // calls setCanceledOnTouchOutside on the underlying dialog
        }

        val passwordInput: EditText = dialog.getCustomView().findViewById(R.id.password)
        val progressBarContainer: View = dialog.getCustomView().findViewById(R.id.progressBarContainer)
        var btnOk = dialog.getActionButton(WhichButton.POSITIVE)
        var btnCancel = dialog.getActionButton(WhichButton.NEGATIVE)

        progressBarContainer.visibility = View.GONE
        btnCancel.isEnabled = true
        btnOk.isEnabled = false
        showKeyboard(passwordInput)

        passwordInput.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                btnOk.isEnabled = isPasswordConfinedToPolicy(passwordInput.text.toString())
            }

        })

        btnOk.setOnClickListener {
            // display a progress activity when decrypting file.
            btnOk.isEnabled = false
            btnCancel.isEnabled = false
            progressBarContainer.visibility = View.VISIBLE

            GlobalScope.launch(Dispatchers.IO) {

                //callback(password.toCharArray())
                var decryptedFilePath = FileManager.decryptFileForSharing(passwordInput.text.toString().toCharArray(), file.path)


                launch(Dispatchers.Main) {

                    dialog.dismiss()

                    if (FileManager.isFileExist(decryptedFilePath)) {
                        // succesfully decrypted file
                        sendShareItent(myContext, decryptedFilePath)
                    } else {
                        // fail to encrypt file
                        showMessageDialog(
                            this@MainActivity,
                            R.string.error,
                            R.string.enc_enter_decrypting_password_error
                        ) {}
                    }

                }

            }

        }

    }


    private fun changeEncryptingPassword(item: FileDirItem) {
        // ask for the original password for this file
        val dialog = MaterialDialog(this).show {
            title(R.string.dlg_title_change_encrypting_password)
            customView(R.layout.change_password_view, scrollable = true, horizontalPadding = true)
            positiveButton(R.string.btn_ok)
            negativeButton(android.R.string.cancel)
            cancelable(false)  // calls setCancelable on the underlying dialog
            cancelOnTouchOutside(false)  // calls setCanceledOnTouchOutside on the underlying dialog
            lifecycleOwner(this@MainActivity)
        }

        val originalPasswordInput: EditText =
            dialog.getCustomView().findViewById(R.id.original_password)
        val newPasswordInput: EditText = dialog.getCustomView().findViewById(R.id.new_password)
        val confirmPasswordInput: EditText =
            dialog.getCustomView().findViewById(R.id.confirm_password)
        val dialogProgressBarContainer: View =
            dialog.getCustomView().findViewById(R.id.dialogProgressBarContainer)
        var btnOk = dialog.getActionButton(WhichButton.POSITIVE)
        var btnCancel = dialog.getActionButton(WhichButton.NEGATIVE)

        dialogProgressBarContainer.visibility = View.GONE
        btnCancel.isEnabled = true
        btnOk.isEnabled = false
        showKeyboard(originalPasswordInput)

        newPasswordInput.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                btnOk.isEnabled = isNewPasswordConfinedToPolicy(
                    newPasswordInput.text.toString(),
                    confirmPasswordInput.text.toString()
                )
            }

        })

        confirmPasswordInput.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                btnOk.isEnabled = isNewPasswordConfinedToPolicy(
                    newPasswordInput.text.toString(),
                    confirmPasswordInput.text.toString()
                )
            }

        })

        btnOk.setOnClickListener {
            // display a progress activity when re-encrypting file.
            btnOk.isEnabled = false
            btnCancel.isEnabled = false
            dialogProgressBarContainer.visibility = View.VISIBLE

            GlobalScope.launch(Dispatchers.IO) {

                // Pull the password out of the custom view when the positive button is pressed
                val oldPassword = originalPasswordInput.text.toString()
                val newPassword = newPasswordInput.text.toString()

                var success = false

                if (isPasswordConfinedToPolicy(oldPassword) && isPasswordConfinedToPolicy(
                        newPassword
                    )
                )
                    success = FileManager.reencryptFiles(
                        item.path,
                        oldPassword.toCharArray(),
                        newPassword.toCharArray()
                    )

                launch(Dispatchers.Main) {
                    dialogProgressBarContainer.visibility = View.GONE
                    if (success)
                        showMessageDialog(
                            this@MainActivity,
                            R.string.dlg_title_success,
                            R.string.dlg_msg_change_encrypting_password_success
                        ) {}
                    else
                        showMessageDialog(
                            this@MainActivity,
                            R.string.dlg_title_error,
                            R.string.dlg_msg_change_encrypting_password_failed
                        ) {}

                    dialog.dismiss()
                }
            }
        }

    }


    private fun refreshFileItemList() {
        showProgressBar(true)
        GlobalScope.launch(Dispatchers.IO) {
            fileItemViewModel.refresh()
            launch(Dispatchers.Main) {
                showProgressBar(false)
            }
        }
    }


    private fun showProgressBar(show: Boolean) {
        if (show) {
            window.setFlags(
                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
            )

            mainActivityProgressBarContainer.visibility = View.VISIBLE
            itemListRecyclerView.visibility = View.GONE
        } else {
            window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)

            mainActivityProgressBarContainer.visibility = View.GONE
            itemListRecyclerView.visibility = View.VISIBLE
        }

    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, resultData: Intent?) {
        super.onActivityResult(requestCode, resultCode, resultData)
        // Check which request we're responding to
        if (requestCode == REQUEST_CODE_TO_ENCRYPT_FILE) {

            if (resultCode == Activity.RESULT_OK) {

                // file encrypted
                var filename = ""
                if (resultData != null) {
                    filename = "\n\n" + resultData.getStringExtra(INTENT_ENCRYPTED_FILENAME)
                }

                showMessageDialog(
                    this,
                    getString(R.string.enc_title_encrypting_file),
                    getString(R.string.enc_msg_encrypting_success) + filename
                ) {
                    refreshFileItemList()
                }

            } else if (resultCode == INTENT_RESULT_FAILED) {
                // failed to encrypt file
                showMessageDialog(
                    this,
                    R.string.enc_title_encrypting_file,
                    R.string.enc_msg_encrypting_failed
                ) {}

            } else if (resultCode == Activity.RESULT_CANCELED) {
                // User cancelled encrypting file
                showMessageDialog(
                    this,
                    R.string.enc_title_encrypting_file,
                    R.string.enc_msg_encrypting_cancelled
                ) {}

            }

        } else if (requestCode == REQUEST_CODE_TO_IMPORTDECRYPT_FILE) {

            if (resultCode == INTENT_RESULT_IMPORTED) {
                // file imported
                var filename = ""
                if (resultData != null) {
                    filename = "\n\n" + resultData.getStringExtra(INTENT_IMPORTED_FILENAME)
                }

                showMessageDialog(
                    this,
                    getString(R.string.imp_title_import_decrypt),
                    getString(R.string.imp_msg_import_success) + filename
                ) {
                    refreshFileItemList()
                }

            } else if (resultCode == INTENT_RESULT_DECRYPTED) {
                // successfully decrypted and shared the file
                // so nothing to do here and quit
                finishAndRemoveTask()

            } else if (resultCode == INTENT_RESULT_FAILED) {
                // import/decrypt operation failed
                showMessageDialog(
                    this,
                    R.string.imp_title_import_decrypt,
                    R.string.imp_msg_import_failed
                ) {
                    finishAndRemoveTask()
                }

            } else if (resultCode == Activity.RESULT_CANCELED) {
                // User cancelled import/decrypt operation
                showMessageDialog(
                    this,
                    R.string.imp_title_import_decrypt,
                    R.string.operation_cancelled
                ) {
                    finishAndRemoveTask()
                }

            }

        }
    }


}
