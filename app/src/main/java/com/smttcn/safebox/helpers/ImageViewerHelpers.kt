package com.smttcn.safebox.helpers

import android.content.Context
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.EditText
import android.widget.ImageView
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.WhichButton
import com.afollestad.materialdialogs.actions.getActionButton
import com.afollestad.materialdialogs.customview.customView
import com.afollestad.materialdialogs.customview.getCustomView
import com.smttcn.commons.extensions.*
import com.smttcn.commons.manager.FileManager
import com.smttcn.commons.manager.ImageManager
import com.smttcn.commons.models.FileDirItem
import com.smttcn.safebox.MyApplication
import com.smttcn.safebox.R
import com.stfalcon.imageviewer.StfalconImageViewer
import kotlinx.android.synthetic.main.recyclerview_item.view.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.File

class ImageViewerHelpers(context: Context, view: View, file: FileDirItem): BaseViewerHelpers(context, view, file) {


    override fun view() {

        // ask for the decrypting password for this file
        val dialog = MaterialDialog(_parentContext).show {
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
        _parentContext.showKeyboard(passwordInput)

        passwordInput.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                btnOk.isEnabled = _parentContext.isPasswordConfinedToPolicy(passwordInput.text.toString())
            }

        })

        btnOk.setOnClickListener {
            // display a progress activity when decrypting file.
            GlobalScope.launch(Dispatchers.Main) {

                btnOk.isEnabled = false
                btnCancel.isEnabled = false
                progressBarContainer.visibility = View.VISIBLE

            }.invokeOnCompletion {

                GlobalScope.launch(Dispatchers.IO) {

                    //Thread.sleep(5000)
                    // Pull the password out of the custom view when the positive button is pressed
                    val password = passwordInput.text.toString()
                    val decryptedFileByteArray = FileManager.decryptFileContentToByteArray(File(_file.path), password.toCharArray())

                    GlobalScope.launch(Dispatchers.Main) {

                        dialog.dismiss()

                        if (decryptedFileByteArray != null) {

                            val imagePaths = listOf(decryptedFileByteArray)
                            StfalconImageViewer.Builder<ByteArray>(
                                _parentContext,
                                imagePaths,
                                ::loadImage
                            )
                                .withTransitionFrom(_view.item_background)
                                .show()

                        } else {

                            // fail to encrypt file
                            showMessageDialog(
                                _parentContext,
                                R.string.error,
                                R.string.enc_enter_decrypting_password_error
                            ) {}

                        }

                    }

                }

            }

        }
    }


    override fun isSupported(): Boolean {
        if (_file.getOriginalFilename().isImageSlow())
            return true
        else
            return false
    }


    private fun loadImage(imageView: ImageView, imageByteArray: ByteArray) {
        val aniFade = AnimationUtils.loadAnimation(_parentContext.applicationContext, R.anim.fadein)
        imageView.startAnimation(aniFade)
        imageView.setImageDrawable(_parentContext.getDrawableCompat(R.drawable.ic_image_gray_24dp))

        if (imageByteArray.size > 0) {
            imageView.setImageBitmap(ImageManager.toBitmap(imageByteArray))
        }

    }


}