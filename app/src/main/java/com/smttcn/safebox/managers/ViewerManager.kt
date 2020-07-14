package com.smttcn.safebox.managers

import android.app.Activity
import android.app.Application
import android.content.Context
import android.view.View
import com.smttcn.commons.extensions.getFileExtension
import com.smttcn.commons.extensions.getMimeType
import com.smttcn.commons.extensions.isImageMimeType
import com.smttcn.commons.extensions.withLeadingCharacter
import com.smttcn.commons.helpers.PdfExtensions
import com.smttcn.commons.helpers.photoExtensions
import com.smttcn.commons.models.FileDirItem
import com.smttcn.safebox.helpers.BaseViewer
import com.smttcn.safebox.helpers.ImageViewer
import kotlin.reflect.KClass


// a viewer manager which manage all types of file viewer helpers
// each viewer helper will register itself to the manager and the manager will
// then be able to decide if a file type is viewer within this app and call the
// appropriate viewer helper accordingly
// .

object ViewerManager{

    var viewerMap: HashMap<String, Array<String>> = hashMapOf()

    init {
        viewerMap["com.smttcn.safebox.helpers.ImageViewer"] = photoExtensions
        viewerMap["com.smttcn.safebox.helpers.PdfViewer"] = PdfExtensions
    }


    fun hasSupportedViewer(file: FileDirItem): Boolean {

        val fileExt = file.getOriginalFilename().getFileExtension()

        for (item in viewerMap.entries) {
            if (item.value.contains(fileExt.withLeadingCharacter('.'))) {
                return true
            }
        }


        return false
    }


    fun getHelper(activity: Activity, view: View, file: FileDirItem): BaseViewer? {

        var helper: BaseViewer? = null
        val fileExt = file.getOriginalFilename().getFileExtension()

        for (item in viewerMap.entries) {
            if (item.value.contains(fileExt.withLeadingCharacter('.'))) {

                helper = Class.forName(item.key).newInstance() as BaseViewer
                helper.initialize(activity, view, file)

            }
        }

        return helper
    }
}