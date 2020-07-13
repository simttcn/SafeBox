package com.smttcn.safebox.managers

import android.content.Context
import android.view.View
import com.smttcn.commons.extensions.getMimeType
import com.smttcn.commons.extensions.isImageMimeType
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

    var viewerMap: HashMap<String, String> = hashMapOf()

    init {
        viewerMap["image"] = "com.smttcn.safebox.helpers.ImageViewer"
    }


    fun hasSupportedViewer(file: FileDirItem): Boolean {

        val mimeType = file.getOriginalFilename().getMimeType().split('/')[0]

        for (item in viewerMap.entries) {
            if (item.key.equals(mimeType, true)) {
                return true
            }
        }


        return false
    }


    fun getHelper(context: Context, view: View, file: FileDirItem): BaseViewer? {

        var helper: BaseViewer? = null
        val mimeType = file.getOriginalFilename().getMimeType().split('/')[0]

        // todo next: loop through viewerMap to decide which helpers to return
        for (item in viewerMap.entries) {
            if (item.key.equals(mimeType, true)) {

                helper = Class.forName(item.value).newInstance() as BaseViewer
                helper.initialize(context, view, file)

            }
        }

        return helper
    }
}