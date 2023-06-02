package com.smttcn.safebox.helpers

import android.app.Activity
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.ImageView
import com.smttcn.commons.extensions.*
import com.smttcn.commons.manager.FileManager
import com.smttcn.commons.manager.ImageManager
import com.smttcn.commons.models.FileDirItem
import com.smttcn.safebox.R
import com.smttcn.safebox.databinding.RecyclerviewItemBinding
import com.stfalcon.imageviewer.StfalconImageViewer
//import kotlinx.android.synthetic.main.recyclerview_item.view.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.File

class ImageViewer : BaseViewer() {


    override fun initialize(activity: Activity, view: View, binding: RecyclerviewItemBinding, fileDirItem: FileDirItem) {
        _parentActivity = activity
        _view = view
        _binding = binding
        _file = fileDirItem
    }


    override fun view(password: CharArray) {

        val decryptedFileByteArray = FileManager.decryptFileToByteArray(File(_file.path), password)

        GlobalScope.launch(Dispatchers.Main) {

            if (decryptedFileByteArray != null) {

                val imagePaths = listOf(decryptedFileByteArray)
                StfalconImageViewer.Builder<ByteArray>(
                    _parentActivity,
                    imagePaths,
                    ::loadImage
                )
                    .withTransitionFrom(_binding.itemBackground)
                    .show()

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