package com.smttcn.commons.extensions

import android.widget.ImageView
import com.squareup.picasso.Picasso
import java.io.File

fun ImageView.loadImage(filePath: String?) =
    Picasso.get().load(File(filePath)).into(this)
//    GlideApp.with(context)
//        .load(url ?: "")
//        .into(this)