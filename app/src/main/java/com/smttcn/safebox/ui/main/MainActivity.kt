package com.smttcn.safebox.ui.main

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Parcelable
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.Toast
import androidx.core.content.FileProvider
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.afollestad.materialdialogs.MaterialDialog
import com.google.android.material.snackbar.Snackbar
import com.smttcn.commons.Manager.FileManager
import com.smttcn.commons.Manager.ImageManager
import com.smttcn.commons.activities.BaseActivity
import com.smttcn.commons.extensions.getDrawableCompat
import com.smttcn.commons.extensions.getFilenameFromPath
import com.smttcn.commons.extensions.toast
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

        recyclerViewAdapter.onItemClick = { view, item ->
            onRecyclerItemClicked(view, item)
        }

        fileItemViewModel = ViewModelProviders.of(this).get(FileItemViewModel::class.java)

        fileItemViewModel.allFileItems.observe(this, Observer { item ->
            // Update the cached copy of the fileItems in the adapter.
            item?.let { recyclerViewAdapter.setFileItems(it) }
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
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId

        when(id) {
            R.id.menu_share -> {
                shareItem()
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

    private fun onRecyclerItemClicked(view: View, item : FileDirItem) {
        // Todo: to ask for password and them open it

        toast(item.filename + " clicked")
//        val targetfile = item.path
//        if (targetfile.length > 0) {
//            val imagePaths = listOf(targetfile)
//            StfalconImageViewer.Builder<String>(this, imagePaths, password, ::loadImage)
//                .withTransitionFrom(view.item_background)
//                .show()
//        }
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

    // todo: to further define the share routine to share all type of files
    private fun shareItem() {

        //todo: choose to share file decrypted or as it is
        var password : CharArray = TEMP_PASSWORD.toCharArray()

        val files = FileManager.getFilesInDocumentRoot()

        if (files.isEmpty()) return

        var decryptedFilepath: String = ""
        val filename = files[0].name.getFilenameFromPath()
        val filepath = FileManager.toFullPathInDocumentRoot(filename)
        val targetfile = File(filepath)

        val targetpath = FileManager.getFolderInCacheFolder("temp_file_share", true)
        if (targetfile.length() > 0 && targetpath != null) {
            decryptedFilepath = FileManager.decryptFile(targetfile, password, targetpath.canonicalPath, false)
        }

        if (decryptedFilepath.isNotEmpty()) {
            val AUTHORITY = "com.simttcn.safebox.fileprovider"

            val file = File(decryptedFilepath)
            val contentUri = FileProvider.getUriForFile(applicationContext, AUTHORITY, file)
            // create new Intent
            val sharingIntent = Intent(Intent.ACTION_SEND)
            // set flag to give temporary permission to external app to use your FileProvider
            sharingIntent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            // I am opening a PDF file so I give it a valid MIME type
            sharingIntent.setDataAndType(contentUri, "image/jpeg");
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
                MaterialDialog(this).show {
                    title(R.string.enc_title_encrypting_file)
                    message(R.string.enc_msg_encrypting_success)
                    positiveButton(R.string.btn_ok)
                    cancelable(false)  // calls setCancelable on the underlying dialog
                    cancelOnTouchOutside(false)  // calls setCanceledOnTouchOutside on the underlying dialog
                }

            } else if (resultCode == Activity.RESULT_CANCELED){

                // User cancelled encrypting file
                MaterialDialog(this).show {
                    title(R.string.enc_title_encrypting_file)
                    message(R.string.enc_msg_encrypting_cancelled)
                    positiveButton(R.string.btn_ok)
                    cancelable(false)  // calls setCancelable on the underlying dialog
                    cancelOnTouchOutside(false)  // calls setCanceledOnTouchOutside on the underlying dialog
                }

            }
        }
    }
}
