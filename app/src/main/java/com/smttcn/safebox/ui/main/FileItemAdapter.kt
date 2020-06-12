package com.smttcn.safebox.ui.main

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.smttcn.commons.Manager.ImageManager
import com.smttcn.safebox.R
import com.smttcn.commons.models.FileDirItem
import kotlinx.android.synthetic.main.recyclerview_item.view.*

class FileItemAdapter internal constructor(context: Context) : RecyclerView.Adapter<FileItemAdapter.FileItemViewHolder>() {

    private val inflater: LayoutInflater = LayoutInflater.from(context)
    private var fileItems = emptyList<FileDirItem>() // Cached copy of FileDirItems
    var onItemClick: ((View, FileDirItem) -> Unit)? = null // to point to the onItemClick method in MainActivity
    val currentContext = context

    override fun getItemCount() = fileItems.size

    inner class FileItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        init {
            itemView.setOnClickListener{
                // invoke the onItemClick method in MainActivity
                onItemClick?.invoke(it, fileItems[adapterPosition])
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FileItemViewHolder {
        val inflatedView = inflater.inflate(R.layout.recyclerview_item, parent, false)
        return FileItemViewHolder(inflatedView)
    }

    override fun onBindViewHolder(holder: FileItemViewHolder, position: Int) {
        val current = fileItems[position]
        holder.bindItem(currentContext, current)
    }

    internal fun setFileItems(items: List<FileDirItem>) {
        this.fileItems = items
        notifyDataSetChanged()
    }

}

@Suppress("UNUSED_PARAMETER")
fun FileItemAdapter.FileItemViewHolder.bindItem(context: Context, item: FileDirItem) {
    if (item.thumbnailDrawable != null)
        itemView.item_thumbnail.setImageDrawable(item.thumbnailDrawable)
    else
        itemView.item_thumbnail.setImageResource(R.drawable.ic_image_gray_24dp)

    itemView.item_name.text = item.filename
}