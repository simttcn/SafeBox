package com.smttcn.safebox.helpers

import android.app.Activity
import android.content.Intent
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.ImageView
import androidx.core.content.ContextCompat.startActivity
import com.smttcn.commons.extensions.getDrawableCompat
import com.smttcn.commons.extensions.showMessageDialog
import com.smttcn.commons.helpers.INTENT_VIEW_FILE_PATH
import com.smttcn.commons.manager.FileManager
import com.smttcn.commons.manager.ImageManager
import com.smttcn.commons.models.FileDirItem
import com.smttcn.safebox.R
import com.smttcn.safebox.databinding.RecyclerviewItemBinding
import com.smttcn.safebox.ui.main.PdfViewActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class PdfViewer : BaseViewer() {


    override fun initialize(activity: Activity, view: View, binding: RecyclerviewItemBinding, fileDirItem: FileDirItem) {
        _parentActivity = activity
        _view = view
        _binding = binding
        _file = fileDirItem
    }


    override fun view(password: CharArray) {

        val decryptedFilePath = FileManager.decryptFileForSharing(_file.path, password)

        GlobalScope.launch(Dispatchers.Main) {

            if (decryptedFilePath.isNotEmpty()) {

                val viewerIntent = Intent(_parentActivity, PdfViewActivity::class.java)
                viewerIntent.putExtra(INTENT_VIEW_FILE_PATH, decryptedFilePath)

                startActivity(_parentActivity, viewerIntent,null)

            } else {

                // fail to encrypt file
                showMessageDialog(
                    _parentActivity,
                    R.drawable.ic_warning,
                    R.string.error,
                    R.string.enc_enter_decrypting_password_error
                ) {}

            }

        }

    }


    private fun loadImage(imageView: ImageView, imageByteArray: ByteArray) {
        val aniFade = AnimationUtils.loadAnimation(_parentActivity.applicationContext, R.anim.fadein)
        imageView.startAnimation(aniFade)
        imageView.setImageDrawable(_parentActivity.getDrawableCompat(R.drawable.ic_image))

        if (imageByteArray.isNotEmpty()) {
            imageView.setImageBitmap(ImageManager.toBitmap(imageByteArray))
        }

    }

}