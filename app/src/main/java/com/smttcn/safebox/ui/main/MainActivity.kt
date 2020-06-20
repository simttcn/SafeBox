package com.smttcn.safebox.ui.main

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Parcelable
import android.text.InputType
import android.view.*
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.PopupMenu
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.input.input
import com.google.android.material.snackbar.Snackbar
import com.smttcn.commons.Manager.FileManager
import com.smttcn.commons.Manager.ImageManager
import com.smttcn.commons.activities.BaseActivity
import com.smttcn.commons.extensions.*
import com.smttcn.commons.helpers.*
import com.smttcn.commons.models.FileDirItem
import com.smttcn.safebox.MyApplication
import com.smttcn.safebox.R
import com.smttcn.safebox.ui.debug.DebugconsoleActivity
import com.smttcn.safebox.ui.security.EncryptingActivity
import com.smttcn.safebox.ui.settings.SettingsActivity
import com.smttcn.safebox.viewmodel.FileItemViewModel
import kotlinx.android.synthetic.main.activity_main.*
import java.io.File


class MainActivity : BaseActivity() {

    private lateinit var fileItemViewModel: FileItemViewModel
    private lateinit var recyclerViewAdapter: FileItemAdapter
    private var IsShareFromOtherApp = false
    private lateinit var toolbarMenu: Menu
    private lateinit var myContext: Context

    var BackButtonPressedOnce = false
    var LastPressedBackTime = System.currentTimeMillis()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        fab.setOnClickListener { view ->
            Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                .setAction("Action", null).show()
        }

        initialize()
        initializeUI()

        showProgressBar(false)
    }

    private fun initialize() {
        MyApplication.mainActivityContext = this
        myContext = this

        IsShareFromOtherApp = if (intent?.action == Intent.ACTION_SEND) true else false

        if (IsShareFromOtherApp) {
            IsShareFromOtherApp = false
            handleShareFromOtherApp()
        }
    }

    private fun initializeUI() {
        val recyclerView = findViewById<RecyclerView>(R.id.itemListRecyclerView)
        recyclerViewAdapter = FileItemAdapter(this)
        recyclerView.adapter = recyclerViewAdapter
        recyclerView.layoutManager = LinearLayoutManager(this)

        recyclerViewAdapter.onItemClick = { view, item, prevIdx, currIdx ->
            onRecyclerItemClicked(view, item, prevIdx, currIdx)
        }

        recyclerViewAdapter.onItemPopupMenuClick = { view, item, position ->
            onRecyclerItemPopupMenuClicked(view, item, position)
        }

        fileItemViewModel = ViewModelProviders.of(this).get(FileItemViewModel::class.java)

        fileItemViewModel.allFileItems.observe(this, Observer { item ->
            // Update the cached copy of the fileItems in the adapter.
            item?.let { recyclerViewAdapter.setFileItems(it as MutableList<FileDirItem>) }
        })
    }

    private fun handleShareFromOtherApp(): Boolean {
        var result: Boolean = true

        val newIntent = Intent(this, EncryptingActivity::class.java)
        (intent.getParcelableExtra<Parcelable>(Intent.EXTRA_STREAM) as? Uri)?.let {
            newIntent.putExtra(INTENT_SHARE_FILE_URI, it)
        }
        startActivityForResult(newIntent, REQUEST_CODE_TO_ENCRYPT_FILE)

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
            finishAndRemoveTask()
        }
        super.onBackPressed()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        toolbarMenu = menu

        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId

        when(id) {
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
    private fun onRecyclerItemClicked(view: View, item : FileDirItem, prevIdx: Int, currIdx: Int) {
        // Todo: to decide whether should allow user to open just by taping on it.

        view.setBackgroundColor(ContextCompat.getColor(this, R.color.colorPrimaryLight))
        toast(item.filename + " clicked")

        // update toolbar icon status
        if (recyclerViewAdapter.getSelectedItemCount() > 0) {
            // item selected
        } else {
            // no item selected
        }

//        val targetfile = item.path
//        if (targetfile.length > 0) {
//            val imagePaths = listOf(targetfile)
//            StfalconImageViewer.Builder<String>(this, imagePaths, password, ::loadImage)
//                .withTransitionFrom(view.item_background)
//                .show()
//        }
    }

    private fun onRecyclerItemPopupMenuClicked(view: View, item : FileDirItem, position: Int) {
        var popupMenu = PopupMenu(this, view, Gravity.BOTTOM + Gravity.RIGHT)
        popupMenu.menuInflater.inflate(R.menu.filediritem_popup_menu, popupMenu.menu)

        popupMenu.setOnMenuItemClickListener {
            // todo next: delete item process
            when (it.itemId) {
                R.id.popupmenu_share_encrypt -> {
                    // copy the file to be shared to the designated share folder
                    val shareFilePath = FileManager.copyFileToTempShareFolder(item.path)
                    if (shareFilePath.length > 0) {
                        sendShareItent(shareFilePath)
                    }
                    true
                }
                R.id.popupmenu_share_decrypt -> {
                    shareItemDecrypted(item)
                    true
                }
                R.id.popupmenu_delete_item -> {
                    recyclerViewAdapter.deleteFileItem(position)
                    showMessageDialog(this, R.id.popupmenu_delete_item.toString(), item.filename){}
                    true
                }
                else -> false
            }

        }

        popupMenu.show()

    }

    private fun loadImage(imageView: ImageView, imagePath: String, password: CharArray) {
        val aniFade = AnimationUtils.loadAnimation(applicationContext, R.anim.fadein)
        imageView.startAnimation(aniFade)
        imageView.setImageDrawable(getDrawableCompat(R.drawable.ic_image_gray_24dp))
        val decryptedFileByteArray = FileManager.decryptFileContentToByteArray(File(imagePath), password)
        if (decryptedFileByteArray != null) {
            imageView.setImageBitmap(ImageManager.toBitmap(decryptedFileByteArray))
        }

    }

    private fun shareItemDecrypted(file: FileDirItem) {
        // ask for the decrypting password for this file
        MaterialDialog(this).show {
            title(R.string.enc_encrypting_password)
            message(R.string.enc_msg_decrypting_password)
            input(
                inputType = InputType.TYPE_TEXT_VARIATION_PASSWORD
            ) { _, text ->

                var decryptedFilePath = decryptFileForSharing(text.toString().toCharArray(), file.path)

                if (FileManager.isFileExist(decryptedFilePath)){
                    // succesfully decrypted file
                    sendShareItent((decryptedFilePath))
                } else {
                    // fail to encrypt file
                    showMessageDialog(myContext,
                        R.string.error,
                        R.string.enc_enter_decrypting_password_error){}
                }
            }
            positiveButton(R.string.btn_ok)
            negativeButton(R.string.btn_cancel)
            cancelable(false)  // calls setCancelable on the underlying dialog
            cancelOnTouchOutside(false)  // calls setCanceledOnTouchOutside on the underlying dialog
        }
    }

    fun decryptFileForSharing(pwd: CharArray, filepath: String): String {

        var decryptedFilepath: String = ""
        val targetfile = File(filepath)

        if (!targetfile.exists())
            return decryptedFilepath

        val targetpath = FileManager.getFolderInCacheFolder(TEMP_FILE_SHARE_FOLDER_NAME, true)
        if (targetfile.length() > 0 && targetpath != null) {
            decryptedFilepath = FileManager.decryptFile(targetfile, pwd, targetpath.canonicalPath, false)
        }

        return decryptedFilepath

    }

    private fun sendShareItent(filepath: String) {

        if (filepath.isNotEmpty()) {

            MyApplication.isSharingItem = true

            val AUTHORITY = "com.smttcn.safebox.fileprovider"

            val file = File(filepath)
            val contentUri = FileProvider.getUriForFile(applicationContext, AUTHORITY, file)
            // create new Intent
            val sharingIntent = Intent(Intent.ACTION_SEND)
            // set flag to give temporary permission to external app to use your FileProvider
            sharingIntent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            // I am opening a PDF file so I give it a valid MIME type
            sharingIntent.setDataAndType(contentUri, filepath.getMimeType());
            sharingIntent.putExtra(Intent.EXTRA_STREAM, contentUri);
            // start sharing activity sheet
            startActivity(Intent.createChooser(sharingIntent, "Share Image"));
        }

    }

    fun refreshFileItemList() {
        fileItemViewModel.refresh()
    }

    fun showProgressBar(show: Boolean) {
        if (show) {
            progressBarContainer.visibility = View.VISIBLE
            itemListRecyclerView.visibility = View.GONE
        } else {
            progressBarContainer.visibility = View.GONE
            itemListRecyclerView.visibility = View.VISIBLE
        }
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, resultData: Intent?) {
        super.onActivityResult(requestCode, resultCode, resultData)
        // Check which request we're responding to
        if (requestCode == REQUEST_CODE_TO_ENCRYPT_FILE) {

            if (resultCode == Activity.RESULT_OK) {

                refreshFileItemList()

                // file encrypted
                showMessageDialog(this,
                    R.string.enc_title_encrypting_file,
                    R.string.enc_msg_encrypting_success){}

            } else if (resultCode == Activity.RESULT_CANCELED){
                // User cancelled encrypting file
                showMessageDialog(this,
                    R.string.enc_title_encrypting_file,
                    R.string.enc_msg_encrypting_cancelled){}

            }
        }
    }


}
